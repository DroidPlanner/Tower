package org.droidplanner.android.utils.tracker;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import timber.log.Timber;

import static android.opengl.GLES20.GL_NO_ERROR;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glShaderSource;

/**
 * Created by Aaron Licata on 2/24/2016.
 */
public class TrackerGLUtils {
    public static final String TAG = "TrackerGLUtils";

    class SaveFrameTask extends AsyncTask<byte[], String, String> {
        private int d = 0;
        @Override
        protected String doInBackground(byte[]... jpeg) {
            d = d + 1;
            String filename = "/sdcard/frameX"+ d + ".raw";
            File photo = new File (filename);
            if (photo.exists()) {
                photo.delete();
            }

            try {
                FileOutputStream fos=new FileOutputStream(photo.getPath());

                fos.write(jpeg[0]);
                fos.close();
            }
            catch (java.io.IOException e) {
                Timber.e("SaveFrameTask", "Exception in ARGLRender", e);
            }

            return (null);
        }
    }

    public void dump(ByteBuffer fboBuffer) {
        byte[] bitmapdata = fboBuffer.array();
        new SaveFrameTask().execute(bitmapdata);
    }
    public static void dump2(ByteBuffer fboBuffer) {
        new TrackerGLUtils().dump(fboBuffer);
    }
    public static int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = glCreateShader(type);

        // add the source code to the shader and compile it
        glShaderSource(shader, shaderCode);
        glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
            Timber.e(TAG, "TrackerTLD: Could not compile shader " + type + ":");
            String shaderInfo = GLES20.glGetShaderInfoLog(shader);
            Timber.e(TAG, shaderInfo);
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        return shader;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            Timber.e(glOperation + " TrackerTLD : glError " + error);
            Timber.d(TAG, error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public byte[] getJpegFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

}
