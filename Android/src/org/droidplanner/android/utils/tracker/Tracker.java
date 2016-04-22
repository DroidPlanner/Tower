package org.droidplanner.android.utils.tracker;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import timber.log.Timber;

public class Tracker {

    public static final int OPTIMAL_WIDTH = 320;
    public static final int OPTIMAL_HEIGHT = 240;
    public static final int MAX_POINTS = 15*15; // number of features to send to tracker
    public static final int MAX_INTS_FOR_NATIVE_DATA = 10+1+4*MAX_POINTS;
    private static final int SIZE_OF_INT = 4;

    public int bbx[] = new int[Tracker.MAX_INTS_FOR_NATIVE_DATA];
    public volatile int selection[] = new int[4];


    public int status = 0;
    public int numPoints = 0;
    public int[] P0 = new int [MAX_POINTS*2];
    public int[] P1 = new int [MAX_POINTS*2];

    private boolean isInited = false;
    static {
        System.loadLibrary("tracker-jni");
    }

    // JNI interface to cpp tracker code
    public native boolean nativeInit(ByteBuffer matAddrRgba, int width, int height,
                                  IntBuffer bb,
                                  int debugLevel);

    public native void nativeDetect(ByteBuffer matAddrRgba, int width, int height,
                                    IntBuffer bb,
                                    int cmd,
                                    int debugLevel);
    public Tracker()
    {
        status = 0;
        numPoints = 0;

        isInited = false;

        bbx[0] = 0;
        bbx[1] = 0;
        bbx[2] = 0;
        bbx[3] = 0;

        selection[0] = 0;
        selection[1] = 0;
        selection[2] = 0;
        selection[3] = 0;
    }

    public void requestInit(int x, int y, int w, int h)
    {
        selection[0] = x;
        selection[1] = y;
        selection[2] = w;
        selection[3] = h;

        isInited = false;
    }

    public boolean isInitialized()
    {
        return isInited;
    }

    public boolean initFrame(ByteBuffer frame, int w, int h, IntBuffer comBuffer)
    {
        int t[] = new int[MAX_INTS_FOR_NATIVE_DATA];
        comBuffer.get(t);
        comBuffer.rewind();

        boolean ok = nativeInit(frame, w, h, comBuffer, 0);

        comBuffer.get(t);
        comBuffer.rewind();

        status = t[5];

        numPoints = t[10];
        P0 = new int [numPoints*2];
        P1 = new int [numPoints*2];

        for (int  i = 0; i < numPoints; i++)
        {
            int cix = 10 + 1 + i*4;
            P0[i*2+0] = t[cix+0];
            P0[i*2+1] = t[cix+1];
            P1[i*2+0] = t[cix+2];
            P1[i*2+1] = t[cix+3];
            //Timber.d("P=%d,%d", P0[i*2+0], P0[i*2+1]);
        }

        if (ok)
        {
            isInited = true;
        }

        return ok;
    }

    /* Factory function to load a texture from the APK. */
    public boolean processFrame(ByteBuffer frame, int w, int h, IntBuffer comBuffer, byte cmd)
    {
        int t[] = new int[10+1+4*MAX_POINTS];  //bbx.position()];
        t[10] = MAX_POINTS; // ask these main points to the tracker
        t[9] = (int)cmd;
        comBuffer.get(t);
        comBuffer.rewind();

        //Timber.d("TrackerTLD res =%d,%d input  bbx[%d,%d,%d,%d] status=%d",
        //        w, h, t[0], t[1], t[2], t[3],
        //        t[5]);

        nativeDetect(frame, w, h, comBuffer, (int) cmd, 0);

        comBuffer.get(t);
        comBuffer.rewind();

        status = t[5];

        numPoints = t[10];
        P0 = new int [numPoints*2];
        P1 = new int [numPoints*2];

        for (int  i = 0; i < numPoints; i++)
        {
            int cix = 10 + 1 + i*4;
            P0[i*2+0] = t[cix+0];
            P0[i*2+1] = t[cix+1];
            P1[i*2+0] = t[cix+2];
            P1[i*2+1] = t[cix+3];
            //Timber.d("P=%d,%d", P0[i*2+0], P0[i*2+1]);
        }
        //Timber.d("TrackerTLD res =%d,%d output bbx[%d,%d,%d,%d] status=%d",
        //       w, h, t[0], t[1], t[2], t[3], t[5]);

        //Timber.d("TrackerTLD results: [%d,%d,%d,%d]", t[0], t[1], t[2], t[3]);

        return false;
    }

    public int[] sendFrameBufferToTracker(ByteBuffer fbBuf,
                                         int fbWidth, int fbHeight,
                                         byte cmdStatus) {
        // @Tracker
        // initialize tracker if not alreaded loaded
        // track selection area at every frame in the updated OGL Surface
        int [] result = new int [4];
        result[0] = result[1] = result[2] = result[2] = 0;

        if (fbBuf != null) {
            IntBuffer bbxBuf;
            ByteBuffer bbb;

            {
                // preparbbxBufe bbox buffer for results
                // 4 ints for bbx, 1 int for paramNums 100 ints for params
                bbb = ByteBuffer.allocateDirect((Tracker.MAX_INTS_FOR_NATIVE_DATA) * SIZE_OF_INT);
                bbb.order(ByteOrder.LITTLE_ENDIAN);
                bbxBuf = bbb.asIntBuffer();

                if (!isInitialized()) {
                    bbx[0] = selection[0];
                    bbx[1] = selection[1];
                    bbx[2] = selection[2];
                    bbx[3] = selection[3];

                    Timber.d("TrackerTLD: ***INIT*** input to Tracker(): [%d,%d,%d,%d]",
                            bbx[0], bbx[1], bbx[2],bbx[3]);

                    bbxBuf.put(bbx);
                    bbxBuf.rewind();

                    // keep initializing the tracker untill selection process succeeds
                    if (initFrame(fbBuf, fbWidth, fbHeight, bbxBuf)) {
                        Timber.d("TrackerTLD: init set DoINIT=false");
                    }
                }

                if (isInitialized()) {
                    processFrame(fbBuf, fbWidth, fbHeight, bbxBuf, cmdStatus);
                    bbxBuf.get(bbx);
                    //tracker.setStatus = b[5]; // none, tracking, detection, learning

                    // return tracker results in fbo coordinate system
                    result[0] = bbx[0];
                    result[1] = bbx[1];
                    result[2] = bbx[2];
                    result[3] = bbx[3];
                }
            }
        }

        return result;
    }

}

