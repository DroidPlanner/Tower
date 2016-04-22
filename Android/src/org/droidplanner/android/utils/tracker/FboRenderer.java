package org.droidplanner.android.utils.tracker;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import org.droidplanner.android.utils.ar.rendering.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import timber.log.Timber;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by Aaron Licata on 2/24/2016.
 */
public class FboRenderer
{
    public static final String TAG = "FboRenderer";

    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 2;

    protected float[] textureFBO = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f};

    private FloatBuffer vertexCoord;
    private FloatBuffer textureCoordFBO;

    protected int mFramebufferWidth; //1920; //1024;
    protected int mFramebufferHeight; //1080; //720;

    protected int mProgramFBO;
    protected int maPositionHandleFBO;
    protected int maTextureHandleFBO;

    protected int mDepthBuffer;


    int framebufferTexture[] = new int[1];
    int fboId[] = new int[1];

    protected int mFramebufferTextureId;
    protected int mFBOId;

    public int id() { return mFBOId;}
    public int getProgram() { return mProgramFBO; }
    public int getWidth() { return mFramebufferWidth; }
    public int getHeight() { return mFramebufferHeight;}
    public int getPositionHandle() { return maPositionHandleFBO; }
    public int getTextureCoordHandle() { return maTextureHandleFBO; }

    public FloatBuffer getTextureCoordData() { return textureCoordFBO;}
    public FloatBuffer getVertexCoordData() { return vertexCoord;}

    private ByteBuffer vBuffer = null;
    protected ByteBuffer pixelBuf = null;



    private static final String VERTEX_SHADER_FBO =
            "attribute vec4 aPosition2;" +
                    "attribute vec4 aTextureCoord2;" +
                    "varying vec2 vTextureCoord;" +
                    "void main() {" +
                    "  gl_Position =  aPosition2;" +
                    "  vTextureCoord = (aTextureCoord2).xy;" +
                    "}";

    private static final String FRAGMENT_SHADER_FBO =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;" +
                    "uniform samplerExternalOES src;" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(src, vTextureCoord);" +
                    "}";

    private static final String FRAGMENT_SHADER_FBO2 =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;" +
                    "uniform samplerExternalOES src;" +
                    "varying vec2 vTextureCoord;" +

                    "vec4 texture2DM(sampler2D src, vec2 uv)"+
                    "{"+
                    "#define s2(a, b)                          temp = a; a = min(a, b); b = max(temp, b);"+
                    "#define t2(a, b)                          s2(v[a], v[b]);"+
                    "#define t24(a, b, c, d, e, f, g, h)        t2(a, b); t2(c, d); t2(e, f); t2(g, h);"+
                    "#define t25(a, b, c, d, e, f, g, h, i, j) t24(a, b, c, d, e, f, g, h); t2(i, j);"+
                    " 	 vec3 v[25];"+
                    "    vec2 invres = vec2(1.0/320.0,1.0/240.0);"+
                    "    vec3 temp;"+
                    "    for(int dX = -2; dX <= 2; ++dX) {"+
                    "        for(int dY = -2; dY <= 2; ++dY) {"+
                    "            vec2 offset = vec2(float(dX), float(dY));"+
                    "            v[(dX + 2) * 5 + (dY + 2)] = (texture2D(src, vTextureCoord + (offset) * invres )).rgb;"+
                    "        }"+
                    "    }"+
                    "    t25(0, 1,   3, 4,      2, 4,      2, 3,      6, 7);"+
                    "    t25(5, 7,   5, 6,      9, 7,      1, 7,      1, 4);"+
                    "    t25(12, 13,      11, 13,      11, 12,      15, 16,      14, 16);"+
                    "    t25(14, 15,      18, 19,      17, 19,      17, 18,      21, 22);"+
                    "    t25(20, 22,      20, 21,      23, 24,      2, 5,      3, 6);"+
                    "    t25(0, 6,   0, 3,      4, 7,      1, 7,      1, 4);"+
                    "    t25(11, 14,      8, 14,      8, 11,      12, 15,      9, 15);"+
                    "    t25(9, 12,      13, 16,      10, 16,      10, 13,      20, 23);"+
                    "    t25(17, 23,      17, 20,      21, 24,      18, 24,      18, 21);"+
                    "    t25(19, 22,      8, 17,      9, 18,      0, 18,      0, 9);"+
                    "    t25(10, 19,      1, 19,      1, 10,      11, 20,      2, 20);"+
                    "    t25(2, 11,      12, 21,      3, 21,      3, 12,      13, 22);"+
                    "    t25(4, 22,      4, 13,      14, 23,      5, 23,      5, 14);"+
                    "    t25(15, 24,      6, 24,      6, 15,      7, 16,      7, 19);"+
                    "    t25(3, 11,      5, 17,      11, 17,      9, 17,      4, 10);"+
                    "    t25(6, 12,      7, 14,      4, 6,      4, 7,      12, 14);"+
                    "    t25(10, 14,      6, 7,      10, 12,      6, 10,      6, 17);"+
                    "    t25(12, 17,      7, 17,      7, 10,      12, 18,      7, 12);"+
                    "    return vec4(v[12],1.0);"+
                    "}"+

                    "void main() {" +
                    "  gl_FragColor = texture2D(src, vTextureCoord);" +
                    "}";

    public FboRenderer()
    {
        float[] vertex = {1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f};
        vertexCoord = ByteBuffer
                .allocateDirect(vertex.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexCoord.put(vertex);
        vertexCoord.position(0);

        textureCoordFBO = ByteBuffer
                .allocateDirect(textureFBO.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureCoordFBO.put(textureFBO);
        textureCoordFBO.position(0);
    }

    public void fboInit(int fbWidth, int fbHeight) {
        mFramebufferWidth = fbWidth; //1920; //1024;
        mFramebufferHeight = fbHeight; //1080; //720;
    }

    void initRBO(int width, int height)
    {
        int[] values = new int[1];

        // Create a depth buffer and bind it.
        GLES20.glGenRenderbuffers(1, values, 0);
        TrackerGLUtils.checkGlError("glGenRenderbuffers");
        mDepthBuffer = values[0];    // expected > 0

        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mDepthBuffer);
        TrackerGLUtils.checkGlError("glBindRenderbuffer " + mDepthBuffer);

        // Allocate storage for the depth buffer.
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,
                GLES20.GL_DEPTH_COMPONENT16,
                width, height);
        TrackerGLUtils.checkGlError("glRenderbufferStorage");

        // Attach the depth buffer to FBO
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER,
                mDepthBuffer);
    }

    void initTO(int width, int height)
    {
        GLES20.glGenTextures(1, framebufferTexture, 0);
        TrackerGLUtils.checkGlError("glGenTextures");
        mFramebufferTextureId = framebufferTexture[0];

        // bind texture to the framebuffer
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFramebufferTextureId);
        TrackerGLUtils.checkGlError("glBindTexture");

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                width, height,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null);
        TrackerGLUtils.checkGlError("glTexImage2D");

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

      /* Specify the texture as an output texture for FBO */
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,
                mFramebufferTextureId, 0);
        TrackerGLUtils.checkGlError("glFramebufferTexture2D");
    }



    public void initFBO(int  width, int height)
    {
        // Generate framebuffer and Textures bind to the framebuffer
        fboInit(width, height);

        GLES20.glGenFramebuffers(1, fboId, 0);
        TrackerGLUtils.checkGlError("glGenFramebuffers");
        mFBOId= fboId[0];

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOId);
        TrackerGLUtils.checkGlError("glBindFramebuffer");

        initTO(width, height);
        initRBO(width, height);

        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Timber.e(TAG, "FRAMEBUFFER NOT COMPLETED");
            throw new RuntimeException("Framebuffer not complete, status="
                    + status);
        }

        // default framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        TrackerGLUtils.checkGlError("glBindFramebuffer");

        // compile shaders and link them into a single FBO program
        int vsFBO = TrackerGLUtils.loadShader(GLES20.GL_VERTEX_SHADER,
                VERTEX_SHADER_FBO);
        int psFBO = TrackerGLUtils.loadShader(GLES20.GL_FRAGMENT_SHADER,
                FRAGMENT_SHADER_FBO);
        mProgramFBO = ShaderHelper.linkProgram(vsFBO, psFBO);
        ShaderHelper.validateProgram(mProgramFBO);

        maPositionHandleFBO = GLES20.glGetAttribLocation(mProgramFBO, "aPosition2");
        TrackerGLUtils.checkGlError("glGetAttribLocation");
        if (maPositionHandleFBO == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition2");
        }
        maTextureHandleFBO = GLES20.glGetAttribLocation(mProgramFBO, "aTextureCoord2");
        TrackerGLUtils.checkGlError("glGetAttribLocation aTextureCoord2");
        if (maTextureHandleFBO == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord2");
        }

        initTrackerBuffers(width, height);
    }

    // @Tracker
    // initialize buffers to store GPU frame buffer into tracker host memory buffer
    public void initTrackerBuffers(int width, int height) {


        pixelBuf = ByteBuffer.allocateDirect(width * height * 4);
        pixelBuf.order(ByteOrder.LITTLE_ENDIAN);
    }

    public ByteBuffer getFBOBuffer() {
        Timber.d(TAG, "TrackerTLD getFBOBuffer");
        if (vBuffer == null) {
            Timber.d(TAG, "TrackerTLD allocateDirect..");
            vBuffer = ByteBuffer.allocateDirect(getWidth() * getHeight() * 4);
            vBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        return vBuffer ;
    }

    public static void drawOffScreenFrame(GL10 gl, FboRenderer fbo, int textureId) {
        final long start = System.nanoTime();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo.id());
        TrackerGLUtils.checkGlError("glBindFramebuffer");

        GLES20.glViewport(0, 0, fbo.getWidth(), fbo.getHeight());
        TrackerGLUtils.checkGlError("glViewport");

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        TrackerGLUtils.checkGlError("glClearColor");

        GLES20.glClear(
                GLES20.GL_DEPTH_BUFFER_BIT |
                        GLES20.GL_COLOR_BUFFER_BIT);
        TrackerGLUtils.checkGlError("glClear");

        GLES20.glUseProgram(fbo.getProgram());
        TrackerGLUtils.checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        TrackerGLUtils.checkGlError("glActiveTexture");

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        TrackerGLUtils.checkGlError("glBindTexture");

        glVertexAttribPointer(fbo.getPositionHandle(),
                POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, 4 * 2,
                fbo.getVertexCoordData());
        TrackerGLUtils.checkGlError("glVertexAttribPointer");

        glEnableVertexAttribArray(fbo.getPositionHandle());
        TrackerGLUtils.checkGlError("glEnableVertexAttribArray");

        glVertexAttribPointer(fbo.getTextureCoordHandle(),
                POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, 4 * 2,
                fbo.getTextureCoordData());
        TrackerGLUtils.checkGlError("glVertexAttribPointer");

        glEnableVertexAttribArray(fbo.getTextureCoordHandle());
        TrackerGLUtils.checkGlError("glEnableVertexAttribArray");

        ///////////////
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        TrackerGLUtils.checkGlError("glDrawArrays");

        GLES20.glFinish();
        TrackerGLUtils.checkGlError("glFinish");

        ByteBuffer fboBuffer = fbo.getFBOBuffer();

        // transfer downsampled image from GPU device memory
        // to CPU host memory
        GLES20.glReadPixels(0, 0,
                fbo.getWidth(), fbo.getHeight(),
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                fboBuffer);
        TrackerGLUtils.checkGlError("glReadPixels");

        //new TrackerGLUtils().dump(fboBuffer);
        TrackerGLUtils.dump2(fboBuffer);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        TrackerGLUtils.checkGlError("glBindFramebuffer");

        Timber.d(TAG,"drawOffScreenFrame() took "
                + ((System.nanoTime() - start) / 1000) + "usec");
    }

}