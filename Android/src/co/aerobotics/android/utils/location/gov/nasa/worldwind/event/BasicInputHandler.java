/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.event;

import android.graphics.Point;
import android.media.*;
import android.opengl.GLES20;
import android.view.*;
import android.view.View;
import android.widget.TextView;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.globes.Globe;

/**
 * @author ccrick
 * @version $Id$
 */
public class BasicInputHandler extends WWObjectImpl implements InputHandler
{
    protected WorldWindow eventSource;

    protected float mPreviousX = -1;
    protected float mPreviousY = -1;
    protected int mPrevPointerCount = 0;

    protected float mPreviousX2 = -1;
    protected float mPreviousY2 = -1;
    protected double mPrevPinchWidth = -1;
    protected Angle mPrevPinchAngle = null;

    protected boolean mIsTap = false;
    protected long mLastTap = -1;       // system time in ms of last tap

    // TODO: put this value in a configuration file
    protected static final int SINGLE_TAP_INTERVAL = 300;
    protected static final int DOUBLE_TAP_INTERVAL = 300;
    protected static final int JUMP_THRESHOLD = 100;
    protected static final double PINCH_WIDTH_DELTA_THRESHOLD = 5;
    protected static final Angle PINCH_ROTATE_DELTA_THRESHOLD = Angle.fromDegrees(1);

    protected ToneGenerator tg;

    public BasicInputHandler()
    {
        tg = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
    }

    public WorldWindow getEventSource()
    {
        return this.eventSource;
    }

    public void setEventSource(WorldWindow eventSource)
    {
        this.eventSource = eventSource;
    }

    public boolean onTouch(View view, MotionEvent motionEvent)
    {

        int pointerCount = motionEvent.getPointerCount();

        final float x = motionEvent.getX(0);
        final float y = motionEvent.getY(0);

        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                if (pointerCount == 1)
                    mIsTap = true;

                // display lat-lon under first finger down

                break;
            }

            // all fingers have left the tablet screen
            case MotionEvent.ACTION_UP:
            {
                if (mIsTap && pointerCount == 1)
                {
                    long curTime = System.currentTimeMillis();
                    long timeSinceLastTap = curTime - mLastTap;

                    // double tap has occurred
                    if (mLastTap > 0 && (timeSinceLastTap < DOUBLE_TAP_INTERVAL))
                    {
                        // handle double tap here

                        // play sound to test
                        //tg.startTone(ToneGenerator.TONE_CDMA_CONFIRM);

                        eventSource.invokeInRenderingThread(new Runnable()
                        {
                            public void run()
                            {
                                //handleGoToLocation(x, y);
                                //updateLatLonDisplay();
                            }
                        });

                        mLastTap = 0;
                    }
                    // otherwise, single tap has occurred
                    else if (mLastTap < 0 || timeSinceLastTap > SINGLE_TAP_INTERVAL)
                    {
                        // handle single tap here

                        // play a beep to test
                        // tg.startTone(ToneGenerator.TONE_PROP_BEEP);

                        mLastTap = curTime;      // last tap is now this tap
                    }

                    displayLatLonAtScreenPoint(x, y);
                    ((WorldWindowGLSurfaceView) view).redraw();
                }

                // reset previous variables
                mPreviousX = -1;
                mPreviousY = -1;
                mPreviousX2 = -1;
                mPreviousY2 = -1;
                mPrevPinchWidth = -1;
                mPrevPinchAngle = null;
                mPrevPointerCount = 0;

                break;
            }

            case MotionEvent.ACTION_MOVE:

                float dx = 0;
                float dy = 0;
                if (mPreviousX > -1 && mPreviousY > -1)
                {
                    dx = x - mPreviousX;
                    dy = y - mPreviousY;
                    mIsTap = false;
                }
                // return if detect a new gesture, as indicated by a large jump
                if (Math.abs(dx) > JUMP_THRESHOLD || Math.abs(dy) > JUMP_THRESHOLD)
                    return true;

                float width = view.getWidth();
                float height = view.getHeight();
                // normalize dx, dy with screen width and height, so they are in [0, 1]
                Vec4 velocity = new Vec4(dx / width, dy / height, 0);  // assumes screen space
                final double xVelocity = dx / width;
                final double yVelocity = dy / height;

                if (pointerCount != 2 || mPrevPointerCount != 2)
                {
                    // reset pinch variables
                    mPreviousX2 = -1;
                    mPreviousY2 = -1;
                    mPrevPinchWidth = -1;
                    mPrevPinchAngle = null;
                }

                // interpret the motionEvent
                if (pointerCount == 1 && !mIsTap)
                {
                    eventSource.invokeInRenderingThread(new Runnable()
                    {
                        public void run()
                        {
                            //handlePan(x, y, mPreviousX, mPreviousY);
                            handlePan(xVelocity, yVelocity);
                        }
                    });
                }
                // handle zoom, rotate/revolve and tilt
                else if (pointerCount > 1)
                {
                    boolean upMove = dy > 0;
                    boolean downMove = dy < 0;
                    boolean rightMove = dx > 0;
                    boolean leftMove = dx < 0;

                    float slope = 2;    // arbitrary value indicating a vertical slope
                    if (dx != 0)
                        slope = dy / dx;

                    // separate gestures by number of fingers
                    if (pointerCount == 2)
                    {
                        float x2 = motionEvent.getX(1);
                        float y2 = motionEvent.getY(1);

                        float dx2 = 0;
                        float dy2 = 0;
                        if (mPreviousX > -1 && mPreviousY > -1)
                        {   // delta is only relevant if a previous location exists
                            dx2 = x2 - mPreviousX2;
                            dy2 = y2 - mPreviousY2;
                        }

                        final double xVelocity2 = dx2 / width;
                        final double yVelocity2 = dy2 / height;

                        Vec4 velocity2 = new Vec4(dx2 / width, dy2 / height, 0);  // assumes screen space

                        final float pinchCenterX = (x + x2) / 2;
                        final float pinchCenterY = (y + y2) / 2;
                        double pinchWidth = Math.sqrt(Math.pow((x - x2), 2) + Math.pow((y - y2), 2));

                        // compute angle traversed
                        double dotProduct = velocity.dot3(velocity2);
                        Angle pinchAngle = Angle.fromDegrees(dotProduct);

                        final double deltaPinchWidth = pinchWidth - mPrevPinchWidth;

                        final Angle deltaPinchAngle = computeRotationAngle(x, y, x2, y2,
                            mPreviousX, mPreviousY, mPreviousX2, mPreviousY2);

                        if (mPrevPinchWidth > 0 && Math.abs(deltaPinchWidth) > PINCH_WIDTH_DELTA_THRESHOLD)
                        {
                            eventSource.invokeInRenderingThread(new Runnable()
                            {
                                public void run()
                                {
                                    handlePinchZoom(deltaPinchWidth, pinchCenterX, pinchCenterY);
                                }
                            });
                        }

                        // TODO: prevent this from confusion with pinch-rotate
                        else if ((upMove || downMove) && Math.abs(slope) > 1
                            && (yVelocity > 0 && yVelocity2 > 0) || (yVelocity < 0 && yVelocity2 < 0))
                        {
                            eventSource.invokeInRenderingThread(new Runnable()
                            {
                                public void run()
                                {
                                    handleLookAtTilt(xVelocity, yVelocity);
                                    //handleZoom(0, yVelocity);
                                }
                            });
                        }

                        else if (deltaPinchAngle != null
                            && deltaPinchAngle.degrees > PINCH_ROTATE_DELTA_THRESHOLD.degrees)
                        {
                            eventSource.invokeInRenderingThread(new Runnable()
                            {
                                public void run()
                                {
                                    handlePinchRotate(deltaPinchAngle, pinchCenterX, pinchCenterY);
                                }
                            });
                        }

                        /*
                        else if ((rightMove || leftMove) && Math.abs(slope) < 1)
                        {
                            eventSource.invokeInRenderingThread(new Runnable()
                            {
                                public void run()
                                {
                                    handleLookAtHeading(xVelocity, yVelocity, x, y);
                                    //handleZoom(xVelocity, 0);
                                }
                            });
                        }
                        */

                        mPreviousX2 = x2;
                        mPreviousY2 = y2;
                        mPrevPinchWidth = pinchWidth;
                        mPrevPinchAngle = pinchAngle;
                    }
                    else if (pointerCount == 3)
                    {   /*
                        if ((upMove || downMove) && Math.abs(slope) > 1)
                        {
                            eventSource.invokeInRenderingThread(new Runnable()
                            {
                                public void run()
                                {
                                    handleLookAtTilt(xVelocity, yVelocity);
                                }
                            });
                        }
                        else if ((rightMove || leftMove) && Math.abs(slope) < 1)
                        {
                            eventSource.invokeInRenderingThread(new Runnable()
                            {
                                public void run()
                                {
                                    handleLookAtHeading(xVelocity, yVelocity, x, y);
                                }
                            });
                        }
                        */

                        eventSource.invokeInRenderingThread(new Runnable()
                        {
                            public void run()
                            {
                                handleRestoreNorth(xVelocity, yVelocity);
                            }
                        });
                    }

                    else if (pointerCount > 3)
                    {   /*
                        if (mPrevPointerCount == 4 && (upMove || downMove) && Math.abs(slope) > 1)
                        {
                            eventSource.invokeInRenderingThread(new Runnable()
                            {
                                public void run()
                                {
                                    handleEyeTilt(xVelocity, yVelocity);
                                }
                            });
                        }
                        else if (mPrevPointerCount == 4 && (rightMove || leftMove) && Math.abs(slope) < 1)
                        {
                            eventSource.invokeInRenderingThread(new Runnable()
                            {
                                public void run()
                                {
                                    handleEyeHeading(xVelocity, yVelocity);
                                }
                            });
                        }
                        if (mPrevPointerCount == 5)
                        {
                            eventSource.invokeInRenderingThread(new Runnable()
                            {
                                public void run()
                                {
                                    handleRestoreNorth(xVelocity, yVelocity);
                                }
                            });
                        }
                        */

                        eventSource.invokeInRenderingThread(new Runnable()
                        {
                            public void run()
                            {
                                handleRestoreNorth(xVelocity, yVelocity);
                            }
                        });
                    }
                }

                ((WorldWindowGLSurfaceView) view).redraw();

                mPreviousX = x;
                mPreviousY = y;
                mPrevPointerCount = pointerCount;

                break;
        }

        return true;
    }

    protected void displayLatLonAtScreenPoint(float x, float y)
    {
        // update displayed lat/lon
        BasicView basicview = (BasicView) this.eventSource.getView();
        Globe globe = this.eventSource.getModel().getGlobe();

        TextView latText = ((WorldWindowGLSurfaceView) this.eventSource).getLatitudeText();
        TextView lonText = ((WorldWindowGLSurfaceView) this.eventSource).getLongitudeText();

        if (latText != null && lonText != null)
        {
            Point touchPt = new Point((int) x, (int) y);
            Position touchPosition = new Position();
            if (basicview.computePositionFromScreenPoint(touchPt, globe, touchPosition))
            {
                latText.setText(touchPosition.latitude.toString());
                lonText.setText(touchPosition.longitude.toString());
            }
            else
            {
                latText.setText(" off globe ");
                lonText.setText(" off globe ");
            }
        }
    }

    // given the current and previous locations of two points, compute the angle of the
    // rotation they trace out
    protected Angle computeRotationAngle(float x, float y, float x2, float y2,
        float xPrev, float yPrev, float xPrev2, float yPrev2)
    {
        // can't compute if no previous points
        if (xPrev < 0 || yPrev < 0 || xPrev2 < 0 || yPrev2 < 0)
            return null;

        if ((x - x2) == 0 || (xPrev - xPrev2) == 0)
            return null;

        // 1. compute lines connecting pt1 to pt2, and pt1' to pt2'
        float slope = (y - y2) / (x - x2);
        float slopePrev = (yPrev - yPrev2) / (xPrev - xPrev2);

        // b = y - mx
        float b = y - slope * x;
        float bPrev = yPrev - slopePrev * xPrev;

        // 2. use Cramer's Rule to find the intersection of the two lines
        float det1 = -slope * 1 + slopePrev * 1;
        float det2 = b * 1 - bPrev * 1;
        float det3 = (-slope * bPrev) - (-slopePrev * b);

        // check for case where lines are parallel
        if (det1 == 0)
            return null;

        // compute the intersection point
        float isectX = det2 / det1;
        float isectY = det3 / det1;

        // 3. use the law of Cosines to determine the angle covered

        // compute lengths of sides of triangle created by pt1, pt1Prev and the intersection pt
        double BC = Math.sqrt(Math.pow(x - isectX, 2) + Math.pow(y - isectY, 2));
        double AC = Math.sqrt(Math.pow(xPrev - isectX, 2) + Math.pow(yPrev - isectY, 2));
        double AB = Math.sqrt(Math.pow(x - xPrev, 2) + Math.pow(y - yPrev, 2));

        Vec4 CA = new Vec4(xPrev - isectX, yPrev - isectY, 0);
        Vec4 CB = new Vec4(x - isectX, y - isectY, 0);

        // if one finger stayed fixed, may have degenerate triangle, so use other triangle instead
        if (BC == 0 || AC == 0 || AB == 0)
        {
            BC = Math.sqrt(Math.pow(x2 - isectX, 2) + Math.pow(y2 - isectY, 2));
            AC = Math.sqrt(Math.pow(xPrev2 - isectX, 2) + Math.pow(yPrev2 - isectY, 2));
            AB = Math.sqrt(Math.pow(x2 - xPrev2, 2) + Math.pow(y2 - yPrev2, 2));

            CA.set(xPrev2 - isectX, yPrev2 - isectY, 0);
            CB.set(x2 - isectX, y2 - isectY, 0);

            if (BC == 0 || AC == 0 || AB == 0)
                return null;
        }

        // Law of Cosines
        double num = (Math.pow(BC, 2) + Math.pow(AC, 2) - Math.pow(AB, 2));
        double denom = (2 * BC * AC);
        double BCA = Math.acos(num / denom);

        // use cross product to determine if rotation is positive or negative
        if (CA.cross3(CB).z < 0)
            BCA = 2 * Math.PI - BCA;

        return Angle.fromRadians(BCA);
    }

    /*
    // computes pan using current and previous touch point locations
    protected void handlePan(float x, float y, float xPrev, float yPrev)
    {
        BasicView view = (BasicView) this.eventSource.getView();
        Globe globe = this.eventSource.getModel().getGlobe();

        if (xPrev < 0 || yPrev < 0)
            return;

        Position lookAtPosition = view.getLookAtPosition(globe);

        if (lookAtPosition != null)
        {
            Angle heading = view.getLookAtHeading(globe);
            Angle tilt = view.getLookAtTilt(globe);
            double dist = view.getLookAtDistance(globe);

            Matrix M = view.getModelviewMatrix();
            Vec4 back = new Vec4(M.m31, M.m32, M.m33);

            int[] viewportArray = new int[4];
            GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewportArray, 0);
            Rect viewport = new Rect(viewportArray[0], viewportArray[3],
                viewportArray[2], viewportArray[1]);
            Line newRay = BasicView.computeRayFromScreenPoint(x, y, M, view.getProjectionMatrix(), viewport);

            double parallel = newRay.getDirection().dot3(back);

            Position end = view.computePositionFromScreenPoint(globe, x, y);
            Position start = view.computePositionFromScreenPoint(globe, xPrev, yPrev);
            Angle deltaLat = end.latitude.subtract(start.latitude).multiplyAndSet(10);
            Angle deltaLon = end.longitude.subtract(start.longitude).multiplyAndSet(10);

            lookAtPosition.latitude.addAndSet(deltaLat);
            lookAtPosition.longitude.addAndSet(deltaLon);

            view.setLookAtPosition(lookAtPosition, heading, tilt, dist, globe);
        }
        else
        {

        }
    }
    */

    // computes pan using velocity of swipe motion
    protected void handlePan(double xVelocity, double yVelocity)
    {
        BasicView view = (BasicView) this.eventSource.getView();
        Globe globe = this.eventSource.getModel().getGlobe();

        double panScalingFactor = 0.00001f;
        Position eyePosition = view.getEyePosition(globe);
        Position lookAtPosition = view.getLookAtPosition(globe);

        if (lookAtPosition != null)
        {
            Angle heading = view.getLookAtHeading(globe);
            Angle tilt = view.getLookAtTilt(globe);
            double dist = view.getLookAtDistance(globe);

            double sinHead = Math.sin(-heading.radians);    // trigonometric functions assume CCW rotation
            double cosHead = Math.cos(-heading.radians);

            double newLong = Angle.normalizedDegreesLongitude(lookAtPosition.longitude.degrees
                - (cosHead * xVelocity - sinHead * yVelocity)
                * panScalingFactor * dist);
            double newLat = Angle.normalizedDegreesLatitude(lookAtPosition.latitude.degrees
                + (cosHead * yVelocity + sinHead * xVelocity) * panScalingFactor * dist);
            lookAtPosition.longitude.setDegrees(newLong);
            lookAtPosition.latitude.setDegrees(newLat);

            view.setLookAtPosition(lookAtPosition, heading, tilt, dist, globe);
        }
        else    // use eye position instead.  TODO: make this work better
        {
            Angle heading = view.getEyeHeading(globe);

            double sinHead = Math.sin(-heading.radians);
            double cosHead = Math.cos(-heading.radians);

            double newLong = Angle.normalizedDegreesLongitude(eyePosition.longitude.degrees
                - (cosHead * xVelocity - sinHead * yVelocity)
                * panScalingFactor * eyePosition.elevation);
            double newLat = Angle.normalizedDegreesLatitude(eyePosition.latitude.degrees
                + (cosHead * yVelocity + sinHead * xVelocity) * panScalingFactor * eyePosition.elevation);
            eyePosition.longitude.setDegrees(newLong);
            eyePosition.latitude.setDegrees(newLat);

            view.setEyePosition(eyePosition, globe);
        }

        // TODO: iterate through all pan listeners
    }

    protected void handleZoom(double xVelocity, double yVelocity)
    {
        BasicView view = (BasicView) this.eventSource.getView();
        Globe globe = this.eventSource.getModel().getGlobe();

        double zoomScalingFactor = 8E-1f;
        double zoom = view.getZoom();

        double dist = view.getLookAtDistance(globe);
        if (dist >= 0)       // scale by lookAt distance if possible
            zoom -= (yVelocity + xVelocity) * zoomScalingFactor * dist;
        else
        {   // if not, scale with eye altitude
            Position eyePos = view.getEyePosition(globe);
            zoom -= (yVelocity + xVelocity) * zoomScalingFactor * 5 * eyePos.elevation;
        }
        view.setZoom(zoom);

        // TODO: iterate through all zoom listeners
    }

    protected void handlePinchZoom(double widthDelta, float centerX, float centerY)
    {
        BasicView view = (BasicView) this.eventSource.getView();
        Globe globe = this.eventSource.getModel().getGlobe();

        double zoomScalingFactor = 3E-3f;
        double zoom = view.getZoom();

        double dist = view.getLookAtDistance(globe);
        if (dist >= 0)       // scale by lookAt distance if possible
            zoom += (widthDelta) * zoomScalingFactor * dist;
        else
        {   // if not, scale with eye altitude
            Position eyePos = view.getEyePosition(globe);
            zoom += (widthDelta) * zoomScalingFactor * 5 * eyePos.elevation;
        }
        view.setZoom(zoom);

        // TODO: iterate through all zoom listeners
    }

    protected void handlePinchRotate(Angle rotAngle, float centerX, float centerY)
    {
        BasicView view = (BasicView) this.eventSource.getView();
        Globe globe = this.eventSource.getModel().getGlobe();

        float headingScalingFactor = 200;
        Angle heading = view.getLookAtHeading(globe);

        // don't handle case where no lookAt intersection with globe
        if (heading == null)
            return;

        heading.setDegrees(heading.degrees + rotAngle.degrees);

        Position lookAt = view.getLookAtPosition(globe);
        Angle tilt = view.getLookAtTilt(globe);
        double range = view.getLookAtDistance(globe);

        //view.setLookAtHeading(heading, globe);
        view.setLookAtPosition(lookAt, heading, tilt, range, globe);

        // TODO: iterate through all heading listeners
    }

    protected void handleLookAtTilt(double xVelocity, double yVelocity)
    {
        BasicView view = (BasicView) this.eventSource.getView();
        Globe globe = this.eventSource.getModel().getGlobe();

        double tiltScalingFactor = 100;
        Angle tilt = view.getLookAtTilt(globe);

        // don't handle case where no lookAt intersection with globe
        if (tilt == null)
            return;
        tilt.setDegrees(tilt.degrees + yVelocity * tiltScalingFactor);
        view.setLookAtTilt(tilt, globe);

        // TODO: iterate through all tilt listeners
    }

    protected void handleEyeTilt(double xVelocity, double yVelocity)
    {
        BasicView view = (BasicView) this.eventSource.getView();
        Globe globe = this.eventSource.getModel().getGlobe();

        double tiltScalingFactor = 8E4;
        Angle tilt = view.getEyeTilt(globe);
        Position eyePos = view.getEyePosition(globe);

        tilt.setDegrees(tilt.degrees + yVelocity * tiltScalingFactor * 1 / Math.sqrt(eyePos.elevation));
        view.setEyeTilt(tilt, globe);

        // TODO: iterate through all tilt listeners
    }

    protected void handleLookAtHeading(double xVelocity, double yVelocity, float x, float y)
    {
        BasicView view = (BasicView) this.eventSource.getView();
        Globe globe = this.eventSource.getModel().getGlobe();

        float headingScalingFactor = 200;
        Angle heading = view.getLookAtHeading(globe);

        // don't handle case where no lookAt intersection with globe
        if (heading == null)
            return;

        // get screen height
        int[] viewportArray = new int[4];
        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewportArray, 0);
        int midHeight = viewportArray[3] / 2;

        // rotate clockwise above mid-screen, counter-clockwise below. (0, 0) is screen upper left.
        if (y > midHeight)
            heading.setDegrees(heading.degrees - xVelocity * headingScalingFactor);
        else
            heading.setDegrees(heading.degrees + xVelocity * headingScalingFactor);

        Position lookAt = view.getLookAtPosition(globe);
        Angle tilt = view.getLookAtTilt(globe);
        double range = view.getLookAtDistance(globe);

        //view.setLookAtHeading(heading, globe);
        view.setLookAtPosition(lookAt, heading, tilt, range, globe);

        // TODO: iterate through all heading listeners
    }

    protected void handleEyeHeading(double xVelocity, double yVelocity)
    {
        BasicView view = (BasicView) this.eventSource.getView();
        Globe globe = this.eventSource.getModel().getGlobe();

        float headingScalingFactor = 200;
        Angle heading = view.getEyeHeading(globe);

        heading.setDegrees(heading.degrees + xVelocity * headingScalingFactor);
        view.setEyeHeading(heading, globe);

        // TODO: iterate through all heading listeners
    }

    protected void handleRestoreNorth(double xVelocity, double yVelocity)
    {
        BasicView view = (BasicView) this.eventSource.getView();
        Globe globe = this.eventSource.getModel().getGlobe();

        Position lookAtPosition = view.getLookAtPosition(globe);
        double delta = Math.sqrt(Math.pow(xVelocity, 2) + Math.pow(yVelocity, 2));

        if (lookAtPosition != null)
        {
            Angle lookAtHeading = view.getLookAtHeading(globe);
            Angle lookAtTilt = view.getLookAtTilt(globe);
            double range = view.getLookAtDistance(globe);
            float headingScalingFactor = 5;
            double tiltScalingFactor = 3;

            // interpolate to zero heading and tilt
            lookAtHeading.addAndSet(Angle.fromDegrees(-lookAtHeading.degrees * delta * headingScalingFactor));
            lookAtTilt.addAndSet(Angle.fromDegrees(-lookAtTilt.degrees * delta * tiltScalingFactor));

            view.setLookAtPosition(lookAtPosition, lookAtHeading, lookAtTilt, range, globe);
        }
        else
        {

        }

        // TODO: iterate through all heading listeners
    }

    protected void handleGoToLocation(float x, float y)
    {
        BasicView view = (BasicView) this.eventSource.getView();
        Globe globe = this.eventSource.getModel().getGlobe();

        Position newLookAtPos = new Position();
        if (!view.computePositionFromScreenPoint(new Point((int) x, (int) y), globe, newLookAtPos))
            return;

        Position lookAtPos = view.getLookAtPosition(globe);
        if (lookAtPos != null)
        {
            Angle lookAtHeading = view.getLookAtHeading(globe);
            Angle lookAtTilt = view.getLookAtTilt(globe);
            double range = view.getLookAtDistance(globe);

            view.setLookAtPosition(newLookAtPos, lookAtHeading, lookAtTilt, range, globe);
        }
        else
        {
            Position eyePos = view.getEyePosition(globe);
            Angle zero = Angle.fromDegrees(0);

            view.setLookAtPosition(newLookAtPos, zero, zero, eyePos.elevation, globe);
        }
    }
}