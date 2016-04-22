package org.droidplanner.android.utils.tracker;

import android.opengl.GLES20;
import android.opengl.Matrix;

import timber.log.Timber;

/**
 * Created by Aaron Licata on 2/26/2016.
 */
public class TrackerDrawer
{
    protected TrackerRectangleDrawer mTrackerRectangle;

    protected float alpha = 0.0f;
    protected float alphaPoints = 0.0f;
    protected float alphaLearning = 0.0f;

    protected int bitmaskTracking = 0x1;
    protected int bitmaskDetecting = 0x2;
    protected int bitmaskFusing = 0x4;
    protected int bitmaskLearning = 0x8;

    protected int bitmaskTrackerFailed = 16;
    protected int bitmaskDetectorFailed = 32;
    protected int bitmaskFuserFailed = 64;
    protected int bitmaskLearnerFailed = 128;

    protected float [] rgbTracking = {0.0f, 0.0f, 1.0f};
    protected float [] rgbDetection = {0.0f, 1.0f, 0.2f};
    protected float [] rgbLearning = {0.0f, 1.0f, 1.0f};

    protected float statusR, statusG, statusB;
    protected byte cmdStatus = 7; //1|2|4;
    protected boolean cmdStatusT = true;
    protected boolean cmdStatusD = true;
    protected boolean cmdStatusL = true;
    protected boolean cmdDebug = false;
    public boolean toggleTest = false;

    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mRenderWidth = 0;
    private int mRenderHeight = 0;
    private int mfboWidth = 0;
    private int mfboHeight = 0;

    public volatile int uiSelection[] = new int[4];

    public  TrackerDrawer()
    {
    }

    public boolean getDebugOn() {
        return cmdDebug;
    }
    public byte getCmdStatus() {
        return cmdStatus;
    }

    public int getDisplayWidth() {
        return mDisplayWidth;
    }

    public int getDisplayHeight() {
        return mDisplayHeight;
    }

    public void TrackerDrawerInit(int displayWidth, int displayHeight,
                                  int fboWidth, int fboHeight)
    {
        mDisplayWidth = displayWidth;
        mDisplayHeight = displayHeight;
        mfboWidth = fboWidth;
        mfboHeight  = fboHeight;

        mTrackerRectangle = new TrackerRectangleDrawer();

        // NOTE: might need moving to render draw with other integrations
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void TrackerDrawer_onSurfaceChanged(int w, int h)
    {
        mRenderWidth = w;
        mRenderHeight = h;
    }

    public void resetUiSelection(int x, int y, int dx, int dy)
    {
        alpha = 1.0f;
        uiSelection[0] = (int)x; //toFbX(x);
        uiSelection[1] = (int)y;  //toFbY(y);;
        uiSelection[2] = (int)dx; //toFbX(dx);;
        uiSelection[3] = (int)dy;  //toFbY(dy);;
    }

    public int toDisplayX(int x)
    {
        return x*mDisplayWidth/mfboWidth; //fbo.getWidth();
    }

    public int toDisplayY(int y)
    {
        return y*mDisplayHeight/ mfboHeight; //fbo.getHeight();
    }


    public void toggleCommands(float x, float y)
    {
        int DY = mDisplayHeight/7;
        int DX = mDisplayWidth/10;

        if (x > (mDisplayWidth-DX-2))
        {
            if (y >  1*DY && y <  2*DY)
            {
                cmdStatusT = !cmdStatusT;
            }
            else if (y >  2*DY && y <  3*DY)
            {
                cmdStatusD = !cmdStatusD;
            }
            else if (y >  3*DY && y <  4*DY) {
                cmdStatusL = !cmdStatusL;
            }
            byte B2 = 2;
            byte B3 = 3;
            byte B4 = 4;
            cmdStatus = 0;
            if (cmdStatusT) cmdStatus = 1;
            if (cmdStatusD) cmdStatus |= B2; //(cmdStatus > 0) ? B3 : B2;
            if (cmdStatusL) cmdStatus |= B4;

            Timber.d("TrackerTLD: cmd %d", cmdStatus);

            if (y > mDisplayHeight-DY) {
                cmdDebug = !cmdDebug;
            }
        }
        else if (x < 40 && y < 40)
        {
            cmdStatusT = !cmdStatusT;
            test += 1;
            if (test > 2) test = 0;
        }
    }

    int test = 1;
    int getTest() {
        return test;
    }

    /*
        draw bouding box

        input: top-left corner of bbx and its size
     */
    void drawTrackerPos(int left, int top, int w, int h,
                        float r,float g, float b, float a, boolean fill) {
        float[] mvpMatrix = new float[16];
        Matrix.setIdentityM(mvpMatrix, 0);

        float nw = (float)w/(float)mRenderWidth;
        float nh = (float)h/(float)mRenderHeight;
        float n11x;
        float n11y;
        int cx = left + w / 2;
        int cy = (top) + h / 2;

        n11x = ((float)cx/(float)mRenderWidth -0.5f)*2.0f;
        n11y = (((float)mRenderHeight-(float)cy)/(float)mRenderHeight -0.5f)*2.0f;

        //Timber.d("TrackerTLD draw cx,cy=%d,%d left+w/2=%d, top+w/2=%d",  cx,cy, w/2, h/2);
        //Timber.d("TrackerTLD draw nw=%f, nh=%f", nw, nh);
        //Timber.d("TrackerTLD draw nx=%f, ny=%f", n11x, n11y);

        float sx = nw*2f;
        float sy = nh*2f; // multiply by 2 because vertices range from -+1/2

        mTrackerRectangle.setColor(r, g, b, a);
        Matrix.translateM(mvpMatrix, 0, n11x, n11y, 0f);
        Matrix.scaleM(mvpMatrix, 0, sx, sy, 1);
        mTrackerRectangle.setFill(fill);
        mTrackerRectangle.draw(mvpMatrix);
    }

    void drawTrackerPos(int left, int top, int w, int h,
                        float r,float g, float b, float a) {
        drawTrackerPos(left, top, w, h, r, g, b, a, false);
    }

    void setData(int cx, int  cy, float r,float g, float b, float a,
                 int scale, boolean filled) {
        float[] mvpMatrix = new float[16];
        Matrix.setIdentityM(mvpMatrix, 0);

        int w=scale;
        int h=scale;
        int left = cx - w/2;
        int top = cy - h/2;

        float nw = (float)w/(float)mRenderWidth;
        float nh = (float)h/(float)mRenderHeight;
        float n11x;
        float n11y;

        n11x = ((float)cx/(float)mRenderWidth -0.5f)*2.0f;
        n11y = (((float)mRenderHeight-(float)cy)/(float)mRenderHeight -0.5f)*2.0f;

        float sx = nw*2f;
        float sy = nh*2f; // multiply by 2 because vertices range from -+1/2

        mTrackerRectangle.setColor(r,g,b,a);

        Matrix.translateM(mvpMatrix, 0, n11x, n11y, 0f);
        Matrix.scaleM(mvpMatrix, 0, sx, sy, 1);

        mTrackerRectangle.setFill((filled));
        mTrackerRectangle.draw(mvpMatrix);
    }

    void setData(int cx, int  cy, float r,float g, float b, float a,
                 int scale)
    {
        setData(cx, cy, r, g, b, a, scale, false);
    }

    public void drawStateCmd()
    {
        int DY = mDisplayHeight/7;
        int DX = mDisplayWidth/10;

        int px = mDisplayWidth-DX-1;

        // tracking
        setData(px, DY, rgbTracking[0], rgbTracking[1], rgbTracking[2], 0.8f, DX / 2,
                (cmdStatusT));


        setData(px, 2 * DY, rgbDetection[0], rgbDetection[1], rgbDetection[2], 0.8f, DX / 2,
                (cmdStatusD));

        setData(px, 3 * DY, rgbLearning[0], rgbLearning[1], rgbLearning[2], 0.8f, DX / 2,
                (cmdStatusL));
    }

    void rgbStatus(int status)
    {
        float r,g,b;
        r = 0.0f;
        g=0.0f;
        b=0.0f;

        if ((status & bitmaskTracking) != 0)
        {
            r = rgbTracking[0];
            g = rgbTracking[1];
            b = rgbTracking[2];
        }
        if ((status & bitmaskLearning) != 0)
        {
            r = rgbLearning[0];
            g = rgbLearning[1];
            b = rgbLearning[2];
        }
        if ((status & bitmaskDetecting) != 0)
        {
            r = rgbDetection[0];
            g = rgbDetection[1];
            b = rgbDetection[2];
        }

        statusR = r;
        statusG = g;
        statusB = b;
    }

    void updateLearnPath(int status, int bxFBO, int byFBO)
    {
        if (bxFBO < 1 || byFBO <1)
            return;

        rgbStatus(status);

        double bx = (double)toDisplayX(bxFBO);
        double by = (double)toDisplayY(byFBO);

        double maxSteps = 20;
        double cx = mDisplayWidth/2;
        double cy = mDisplayHeight/2;

        double dx = bx-cx;
        double dy = by-cy;

        double stepX = dx / maxSteps;
        double stepY = dy / maxSteps;

        for (float i = 0; i < maxSteps; i += 1.0f)
        {
            int sx = (int)(cx + stepX * (double)i);
            int sy = (int)(cy + stepY * (double)i);
            setData(sx, sy, statusR, statusG, statusB, 0.8f, 4);
        }
    }

    float getFailureStatus(int status, int mask)
    {
        if ((status & mask) != 0)
            return 0.25f;

        return 1.0f;
    }

    float clampf(float in, float max)
    {
        return in < max ? in : 1.0f;
    }

    public void TrackerDrawer_drawDebug(int status,
                                        int [] bbx,
                                        int [] P0, int [] P1,
                                        int numPoints)
    {
        rgbStatus(status);

        if ((status & bitmaskTracking) != 0) {
            float r= 1.0f - clampf(((float)numPoints)/30.0f, 1.0f);
            float g=1.0f;
            float b=0.5f;
            alphaPoints *= 0.8;
            if (alphaPoints < 0.01) alphaPoints = 1.0f;

            for (int i = 0; i <  numPoints; i++)
            {
                int cx = P0[i*2+0];
                int cy = P0[i*2+1];
                int cx1 = P1[i*2+0];
                int cy1 = P1[i*2+1];

                double dx = cx-cx1;
                double dy = cy-cy1;
                double a = (Math.atan2(dx,dy)+Math.PI)/(Math.PI*2.0);

                float r2 = (float)Math.max(r+0.5,1.0) * (float)a;

                cx1 = toDisplayX(cx1);
                cy1 = toDisplayY(cy1);

                setData(cx1, cy1, r2*0.8f, g*0.8f, b*0.8f,
                        Math.max(0.1f + alphaPoints, 1.0f), 6);

                cx = toDisplayX(cx);
                cy = toDisplayY(cy);
                setData(cx, cy, r2, g, b, 0.8f, 6);
            }
        }

        // center;
        int sw = uiSelection[2]; //toDisplayX(tracker.selection[2]);
        int sh = uiSelection[3]; //toDisplayY(tracker.selection[3]);
        int cx = mDisplayWidth/2 - sw/2;
        int cy = mDisplayHeight/2 - sh/2;
        drawTrackerPos(cx, cy, sw, sh, 0.7f, 0.7f, 0.7f, 0.6f);

        // target selection
        int sx = uiSelection[0]; //toDisplayX(uiSelection[0]);
        int sy = uiSelection[1];// toDisplayY(uiSelection[1]);
        sw = uiSelection[2]; //toDisplayX(uiSelection[2]);
        sh = uiSelection[3];//toDisplayY(uiSelection[3]);
        drawTrackerPos(sx, sy, sw, sh, 0.3f, 1.0f, 0.3f, alpha);
        alpha *= 0.7f;

        updateLearnPath(status,
                bbx[0]+bbx[2]/2,
                bbx[1]+bbx[3]/2);

        drawStateCmd();

        int D = 40;
        int SDX = D+10;

        if ((status) == 0)
        {
            drawTrackerPos(1*SDX, 0*SDX, D,D, 0.0f, 0.0f, 0.0f, 1.0f);
        }

        // TRACKING BLUE
        if ((status & bitmaskTracking) != 0)
        {
            float s = getFailureStatus(status, bitmaskTrackerFailed);
            drawTrackerPos(1*SDX, 0*SDX, D,D, s*0.0f, s*0.1f, s*0.9f, 1.0f);
        }

        // DETECTION GREEN
        if ((status & bitmaskDetecting) != 0)
        {
            float s = getFailureStatus(status, bitmaskDetectorFailed);
            drawTrackerPos(1*SDX, 1*SDX, D,D, s*0.1f, s*1.0f, s*0.3f, 1.0f);
        }

        // FUSING
        if ((status & bitmaskFusing) != 0)
        {
            float s = getFailureStatus(status, bitmaskFuserFailed);
            drawTrackerPos(2*SDX, 2*SDX, D,D, s*1.0f, s*1.0f, s*1.0f, 1.0f);
        }

        // LEARNING
        if ((status & bitmaskLearning) != 0)
        {
            if (bbx[2]>0 && bbx[3]>0) {
                // learning rectangle
                int dispBx = toDisplayX(bbx[0]);
                int dispBy = toDisplayY(bbx[1]);
                int dispBdx = toDisplayX(bbx[2]);
                int dispBdy = toDisplayY(bbx[3]);

                alphaLearning *= 0.5;
                if (alphaLearning < 0.0000001) alphaLearning = 0.2f;
                drawTrackerPos(dispBx - 2, dispBy - 2, dispBdx - 2, dispBdy - 2,
                        0.2f, 1.0f, 1.0f,
                        Math.max(alphaLearning, 0.1f), true);
            }
            float s = getFailureStatus(status, bitmaskLearnerFailed);
            drawTrackerPos(1*SDX, 3*SDX, D,D, s*0.8f, s*1.0f, s*1.0f, 1.0f);
        }
    }

    public void TrackerDrawer_draw(int status, int [] bbx)
    {
        int dispBx = toDisplayX(bbx[0]);
        int dispBy = toDisplayY(bbx[1]);
        int dispBdx = toDisplayX(bbx[2]);
        int dispBdy = toDisplayY(bbx[3]);

        if (bbx[2]>0 && bbx[3]>0) {

            float r = 0.2f;
            float g = 1.0f;
            float b = 0.2f;
            if (getDebugOn()) {
                r = statusR;
                g = statusG;
                b = statusB;
            }

            drawTrackerPos(dispBx, dispBy,
                    dispBdx, dispBdy,
                    0.2f, 1.0f, 0.2f,
                    Math.max(0.9f + alpha, 1.0f));
        }
        else
            drawTrackerPos(1,1,10,10, 1.0f, 0.3f, 0.3f, 1.0f);

    }
}

