package org.droidplanner.android.utils.ar.rendering;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.view.Surface;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.solo.SoloCameraApi;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.model.AbstractCommandListener;

import org.droidplanner.android.R;
import org.droidplanner.android.utils.ar.ARWorld;
import org.droidplanner.android.utils.ar.telemetry.TelemetryUtils;
import org.droidplanner.android.utils.tracker.TrackerManager;
import org.droidplanner.android.utils.tracker.TrackerUiInterface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
/**
 * Created by Aaron Licata on 3/1/2016.
 */
public class TrackerDroneCameraRenderer
        implements GLSurfaceView.Renderer,
    TrackerUiInterface, // needed to link UI to Tracker & TrackerRenderer states
        SurfaceTexture.OnFrameAvailableListener {
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 2;

    // @Tracker integration
    private TrackerManager trackerManager = new TrackerManager();

    int mDisplayWidth = 0;
    int mDisplayHeight = 0;

    // For camera.
    private int[] textureStatus;
    private FloatBuffer vertexCoord;
    private FloatBuffer textureCoord;
    private int program;

    private SurfaceTexture cameraSurfaceTexture;

    private boolean glInit = false;
    private boolean updateCameraSurfaceTexture = false;

    private TrackerSurfaceView arglView;

    private Context context;

    private Drone drone;

    private float[] projectionMatrix = new float[16];

    public TrackerDroneCameraRenderer(Context context, TrackerSurfaceView arglView) {
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
        openCamera();
    }

    public void onPause() {
        glInit = false;
        updateCameraSurfaceTexture = false;

        closeCamera();
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        initCubeObjects();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // @Tracker integration
        Point p = new Point();
        arglView.getDisplay().getRealSize(p);
        mDisplayWidth = p.x;
        mDisplayHeight = p.y;
        trackerManager.onSurfaceCreated(mDisplayWidth, mDisplayHeight);

        initCamera();

        glInit = true;
    }

    // @Tracker integration
    public void setBox(float x, float y, float dx, float dy,  boolean doInit)
    {
        trackerManager.setSelection(x, y, dx, dy, doInit);
    }
    // @Tracker integration
    public void toggleCommands(float x, float y)
    {
        trackerManager.toggleCommands(x,y);
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

        if (mDisplayHeight > mDisplayWidth) {
            GLES20.glViewport(0, 0, mDisplayHeight, mDisplayWidth);
        }
        else
        {
            GLES20.glViewport(0,0,  mDisplayWidth, mDisplayHeight);
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

        /*
        // Draw objects defined in ARWorld.
        List<ARObject> objectsToDraw = ARWorld.getInstance().getObjectsToDraw();
        for (ARObject object : objectsToDraw) {
            object.draw(ARCamera.getInstance().getViewProjectionMatrix());
        }
        */

        // @Tracker integration
        trackerManager.onDrawFrameOnScreen();
        trackerManager.onDrawFrameOffScreen(unused, textureStatus[0]);

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

        // @Tracker integration
        trackerManager.onSurfaceChanged(width, height);
    }

    private void initCamera() {        // Init textures.
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

    public void setDrone(Drone drone) {
        if (this.drone == null) {
            this.drone = drone;
            trackerManager.setDrone(drone);
        }

        openCamera();
    }

    private void openCamera() {
        if (drone != null) {
            try {
                if (cameraSurfaceTexture == null) {
                    Timber.d("Sleeping...");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                // Swallow
            }

            SoloCameraApi.getApi(drone).startVideoStream(new Surface(cameraSurfaceTexture), "", false,
                    new AbstractCommandListener() {
                        @Override
                        public void onSuccess() {
                            Timber.d("Video stream started successfully.");
                        }

                        @Override
                        public void onError(int i) {
                            Timber.d("Error while starting video stream: %d", i);
                        }

                        @Override
                        public void onTimeout() {
                            Timber.d("Timed out while starting video stream.");
                        }
                    });
        } else {
            Timber.d("Unable to start vehicle video stream...");
        }
    }

    private void closeCamera() {
        if (drone != null) {
            SoloCameraApi.getApi(drone).stopVideoStream(null);
        } else {
            Timber.d("Unable to stop vehicle video stream...");
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


