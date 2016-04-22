package org.droidplanner.android.utils.ar.rendering;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import org.droidplanner.android.R;
import org.droidplanner.android.utils.ar.ARWorld;
import org.droidplanner.android.utils.ar.telemetry.TelemetryUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import timber.log.Timber;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

/**
 * Augmented reality OpenGL renderer that draws objects on the device camera video stream.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ARDeviceCameraRenderer implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener {
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 2;

    // For camera.
    private int[] textureStatus;
    private FloatBuffer vertexCoord;
    private FloatBuffer textureCoord;
    private int program;

    private SurfaceTexture cameraSurfaceTexture;

    private boolean glInit = false;
    private boolean updateCameraSurfaceTexture = false;

    private ARSurfaceView arglView;

    private CameraDevice camera;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private String cameraID;
    private Size previewSize = new Size(1920, 1080);

    private HandlerThread cameraThread;
    private Handler cameraHandler;
    private Semaphore cameraOpenCloseLock = new Semaphore(1);

    private Context context;

    private float[] projectionMatrix = new float[16];

    public ARDeviceCameraRenderer(Context context, ARSurfaceView arglView) {
        this.context = context;
        this.arglView = arglView;

        float[] vertex = { 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f };
        vertexCoord = ByteBuffer
                .allocateDirect(vertex.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexCoord.put(vertex);
        vertexCoord.position(0);

        float[] texture = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };
        textureCoord = ByteBuffer
                .allocateDirect(texture.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureCoord.put(texture);
        textureCoord.position(0);
    }

    public void onResume() {
        startCameraThread();
    }

    public void onPause() {
        glInit = false;
        updateCameraSurfaceTexture = false;

        closeCamera();
        stopCameraThread();
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        initCubeObjects();

        initCamera();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Set camera preview size.
        Point p = new Point();
        arglView.getDisplay().getRealSize(p);
        cacPreviewSize(p.x, p.y);

        openCamera();

        glInit = true;
    }

    public void onDrawFrame(GL10 unused) {
        if (!glInit) {
            return;
        }

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        synchronized(this) {
            if (updateCameraSurfaceTexture) {
                cameraSurfaceTexture.updateTexImage();
                updateCameraSurfaceTexture = false;
            }
        }

        glUseProgram(program);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        int ph = glGetAttribLocation(program, "vPosition");
        glVertexAttribPointer(ph, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 4 * 2, vertexCoord);
        glEnableVertexAttribArray(ph);

        int tch = glGetAttribLocation(program, "vTexCoord");
        glVertexAttribPointer(tch, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 4 * 2, textureCoord);
        glEnableVertexAttribArray(tch);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureStatus[0]);
        glUniform1i(glGetUniformLocation(program, "sTexture"), 0);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        // Draw objects defined in ARWorld.
        List<ARObject> objectsToDraw = ARWorld.getInstance().getObjectsToDraw();
        for (ARObject object : objectsToDraw) {
            object.draw(ARCamera.getInstance().getViewProjectionMatrix());
        }
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
    }

    private void initCamera() {
        // Init textures.
        textureStatus = new int[1];
        glGenTextures(1, textureStatus, 0);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureStatus[0]);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        cameraSurfaceTexture = new SurfaceTexture(textureStatus[0]);
        cameraSurfaceTexture.setOnFrameAvailableListener(this);

        // Init shaders.
        int vertexShader = ShaderHelper.compileVertexShader(TextResourceReader
                .readTextFileFromResource(context, R.raw.simple_vertex_shader));
        int fragmentShader = ShaderHelper.compileFragmentShader(TextResourceReader
                .readTextFileFromResource(context, R.raw.simple_fragment_shader));

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        ShaderHelper.validateProgram(program);
    }

    public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
        // Got a new video frame. Get the closest telemetry data in time and compute camera view projection matrix.
        long currentTime = System.currentTimeMillis();
        ARWorld.getInstance().getTelemetryToVideoSyncer().computeARCameraProjectionMatrix(currentTime);

        updateCameraSurfaceTexture = true;
        arglView.requestRender();
    }

    private void cacPreviewSize(final int width, final int height) {
        CameraManager manager =
                (CameraManager) arglView.getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;

                cameraID = cameraId;
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                for (Size psize : map.getOutputSizes(SurfaceTexture.class)) {
                    if (width == psize.getWidth() && height == psize.getHeight()) {
                        previewSize = psize;
                        break;
                    }
                }
                break;
            }
        } catch (CameraAccessException e) {
            Timber.e(e, "Error when accessing camera...");
        } catch (IllegalArgumentException e) {
            Timber.e(e, "Camera illegal argument error...");
        } catch (SecurityException e) {
            Timber.e(e, "Camera security error...");
        }
    }

    private void openCamera() {
        CameraManager manager =
                (CameraManager) arglView.getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(cameraID, stateCallback, cameraHandler);
        } catch (CameraAccessException e) {
            Timber.e(e, "Error when accessing camera...");
        } catch (IllegalArgumentException e) {
            Timber.e(e, "Camera illegal argument error...");
        } catch (SecurityException e) {
            Timber.e(e, "Camera security error...");
        } catch (InterruptedException e) {
            Timber.e(e, "Interrupted while accessing camera thread...");
        }
    }

    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (null != captureSession) {
                captureSession.close();
                captureSession = null;
            }
            if (null != camera) {
                camera.close();
                camera = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing...", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            camera = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            camera = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            camera = null;
        }

    };

    private void createCameraPreviewSession() {
        try {
            cameraSurfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            Surface surface = new Surface(cameraSurfaceTexture);

            previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            camera.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (null == camera)
                                return;

                            captureSession = cameraCaptureSession;
                            try {
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                                captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, cameraHandler);
                            } catch (CameraAccessException e) {
                                Timber.e(e, "Error when accessing camera...");
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            Timber.e(e, "Error when accessing camera...");
        }
    }

    private void startCameraThread() {
        cameraThread = new HandlerThread("CameraThread");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    private void stopCameraThread() {
        cameraThread.quitSafely();
        try {
            cameraThread.join();
            cameraThread = null;
            cameraHandler = null;
        } catch (InterruptedException e) {
            Timber.e(e, "Interrupted while trying to stop the camera thread...");
        }
    }

    // TODO: Update this method with objects to be drawn when AR starts.
    private void initCubeObjects() {
        // Draw some cubes in front of camera.
        float[] headingOffsets = new float[] { -50.2f, -25.3f, 0.0f, 10.3f, 20.5f };
        float[] objectDistance = new float[] { 20.0f, 30.0f, 100.0f, 60.0f, 7.0f };

        int counter = 0;
        while (counter < headingOffsets.length) {
            LatLongAlt location = TelemetryUtils.newLocationFromAzimuthAndDistance(ARWorld.getInstance().getVehicleLocation(),
                headingOffsets[counter], objectDistance[counter]);

            Cube cube = new Cube(context, location);
            cube.init();

            ARWorld.getInstance().addObjectToDraw(cube);

            ++counter;
        }
    }
}
