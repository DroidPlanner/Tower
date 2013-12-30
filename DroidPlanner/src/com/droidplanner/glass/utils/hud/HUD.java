package com.droidplanner.glass.utils.hud;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;
import com.MAVLink.Messages.ApmModes;
import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.drone.variables.Altitude;
import com.droidplanner.drone.variables.Battery;
import com.droidplanner.drone.variables.GPS;
import com.droidplanner.drone.variables.Orientation;
import com.droidplanner.drone.variables.Speed;
import com.droidplanner.drone.variables.State;
import com.droidplanner.drone.variables.Type;

/**
 * Widget for a HUD Originally copied from http://code.google.com/p/copter-gcs/
 * Modified by Karsten Prange (realbuxtehuder):
 * <p/>
 * - Improved consistency across different screen sizes by replacing all fixed
 * and density scaled size and position values by percentual scaled values
 * <p/>
 * - Added functionality to show dummy data for debugging purposes
 * <p/>
 * - Some minor layout changes
 */
public class HUD extends View implements OnDroneListner {

    // in relation to width (total HUD widget width)
    static final float SCROLLER_WIDTH_FACTOR = .15f;
    // in relation to attHeightPx
    static final float SCROLLER_MAX_HEIGHT_FACTOR = .80f;
    // in relation to attHeightPx
    // in relation to scrollerSizePxText
    static final float SCROLLER_FACTOR_TEXT_Y_OFFSET = -.16f;
    // in relation to the resulting size of SCROLLER_FACTOR_TEXT
    static final float SCROLLER_FACTOR_TEXT_X_OFFSET = .037f;
    // in relation to width
    static final float SCROLLER_FACTOR_TIC_LENGTH = .025f;
    // in relation to scrollerSizePxText
    static final float SCROLLER_FACTOR_ARROW_HEIGTH = 1.4f;
    // in relation to attHeightPx
    static final float SCROLLER_FACTOR_TARGET_BAR_WIDTH = .015f;
    static final int SCROLLER_VSI_RANGE = 12;
    static final int SCROLLER_ALT_RANGE = 26;
    static final int SCROLLER_SPEED_RANGE = 26;

    // in relation to the resulting size of PITCH_FACTOR_TEXT
    static final float PITCH_FACTOR_TEXT_Y_OFFSET = -.16f;
    // in relation to attHeightPx
    static final float PITCH_FACTOR_SCALE_Y_SPACE = 0.02f;
    // in relation to width
    static final float PITCH_FACTOR_SCALE_TEXT_X_OFFSET = 0.025f;

    // in relation to averaged of width and height
    static final float HUD_FACTOR_BORDER_WIDTH = .0075f;
    // in relation to averaged of width and height
    static final float HUD_FACTOR_SCALE_THICK_TIC_STROKEWIDTH = .005f;
    // in relation to averaged of width and height
    static final float HUD_FACTOR_SCALE_THIN_TIC_STROKEWIDTH = .0025f;

    // in relation to rollTopOffsetPx
    static final float ROLL_FACTOR_TIC_LENGTH = .25f;
    // in relation to rollSizePxTics
    static final float ROLL_FACTOR_TEXT_Y_OFFSET = .8f;

    // in relation to yawSizePxText
    static final float YAW_FACTOR_TEXT_Y_OFFSET = -.16f;
    // in relation to yawHeightPx
    static final float YAW_FACTOR_TICS_SMALL = .20f;
    // in relation to yawHeightPx
    static final float YAW_FACTOR_TICS_TALL = .35f;
    // in relation to yawHeightPx
    static final float YAW_FACTOR_CENTERLINE_OVERRUN = .2f;
    static final int YAW_DEGREES_TO_SHOW = 90;

    // in relation to the resulting size of ATT_FACTOR_INFOTEXT
    static final float ATT_FACTOR_INFOTEXT_Y_OFFSET = -.1f;
    // in relation to width
    static final float ATT_FACTOR_INFOTEXT_X_OFFSET = .013f;
    // in relation to attSizePxInfoText
    static final float ATT_FACTOR_INFOTEXT_CLEARANCE = .1f;

    private int width;
    private int height;

    /**
     * Paint used to draw the hud ground.
     */
    private Paint groundPaint;

    /**
     * Paint used to draw the hud sky.
     */
    private Paint skyPaint;

    public int pitchTextCenterOffsetPx;
    public int pitchPixPerDegree;
    public int pitchScaleTextXOffset;

    /**
     * Paint used to draw the hud reticle.
     */
    public Paint reticlePaint;

    /**
     * Radius of the hud's reticle.
     */
    private float reticleRadius;

    public int rollTopOffsetPx;
    public int rollSizePxTics;
    public int rollPosPxTextYOffset;

    /**
     * Paint used to draw the background for the top bar.
     */
    private Paint topBarBgPaint;

    /**
     * Height of the top bar.
     */
    private float topBarHeight;

    public int yawYPosPxText;
    public int yawYPosPxTextNumbers;
    public double yawDegreesPerPixel;
    public int yawSizePxTicsSmall;
    public int yawSizePxTicsTall;
    public int yawSizePxCenterLineOverRun;

    /**
     * Paint used to draw text on the hud.
     */
    private Paint textPaint;

    public int attHeightPx;
    public float attPosPxInfoTextUpperTop;
    public float attPosPxInfoTextUpperBottom;
    public float attPosPxInfoTextLowerTop;
    public float attPosPxInfoTextLowerBottom;
    public float attPosPxInfoTextXOffset;

    /**
     * Padding for the failsafe text.
     */
    private float failsafeTextPadding;

    /**
     * Paint used to draw the failsafe text.
     */
    private Paint failsafeTextPaint;

    /**
     * Paint used to draw the scrollers background.
     */
    private Paint scrollerBgPaint;
    /*
    Scroller's related variables.
     */
    public float scrollerHeightPx;
    public int scrollerWidthPx;
    public int scrollerSizePxTextYOffset;
    public int scrollerSizePxActualTextYOffset;
    public int scrollerSizePxTextXOffset;
    public int scrollerSizePxArrowHeight;
    public int scrollerSizePxTicLength;

    private Paint greenPen;
    private Paint blueVSI;

    /*
    Pitch's related properties
     */
    /**
     * Sets the width of the pitch scale.
     */
    private float pitchScaleWidth;

    /*
    Bottom info bar properties
     */
    /**
     * Paint used to draw the background of the bottom info bar
     */
    private Paint bottomBarBgPaint;

    /**
     * Height of the bottom info bar.
     */
    private float bottomBarHeight;

    /**
     * Vertical padding for elements in the bottom bar.
     */
    private float bottomBarVerticalPadding;

    /**
     * Horizontal padding for elements in the bottom bar.
     */
    private float bottomBarHorizontalPadding;

    /*
    Common paint's variables
     */
    public Paint whiteBorder;
    public Paint whiteThickTics;
    public Paint whiteThinTics;
    public Paint blackSolid;

    /*
    Drone's properties
     */
    private Altitude droneAltitude = new Altitude(null);
    private Battery droneBattery = new Battery(null);
    private GPS droneGPS = new GPS(null);
    private Orientation droneOrientation = new Orientation(null);
    private Speed droneSpeed = new Speed(null);
    private State droneState = new State(null);
    private Type droneType = new Type(null);

    public HUD(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public HUD(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.HUD,
                defStyle, 0);

        try {
            textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(attributes.getColor(R.styleable.HUD_textColor, Color.WHITE));
            textPaint.setTextSize(attributes.getDimension(R.styleable.HUD_textSize, 25f));

            reticlePaint = new Paint();
            reticlePaint.setStyle(Paint.Style.STROKE);
            reticlePaint.setStrokeWidth(3);
            reticlePaint.setAntiAlias(true);
            reticlePaint.setColor(attributes.getColor(R.styleable.HUD_reticleColor, Color.RED));

            reticleRadius = attributes.getDimension(R.styleable.HUD_reticleRadius, 10f);

            groundPaint = new Paint();
            groundPaint.setColor(attributes.getColor(R.styleable.HUD_groundColor, Color.argb(220,
                    148, 193, 31)));

            skyPaint = new Paint();
            skyPaint.setColor(attributes.getColor(R.styleable.HUD_skyColor, Color.argb(220, 0,
                    113, 188)));

            failsafeTextPaint = new Paint();
            failsafeTextPaint.setAntiAlias(true);
            failsafeTextPaint.setTextSize(attributes.getDimension(R.styleable
                    .HUD_failsafeTextSize, 37f));

            failsafeTextPadding = attributes.getDimension(R.styleable.HUD_failsafeTextPadding,
                    25f);

            scrollerBgPaint = new Paint();
            scrollerBgPaint.setColor(attributes.getColor(R.styleable.HUD_scrollerBgColor,
                    Color.argb(64, 255, 255, 255)));

            greenPen = new Paint();
            greenPen.setColor(Color.GREEN);
            greenPen.setStrokeWidth(6);
            greenPen.setStyle(Paint.Style.STROKE);

            blueVSI = new Paint();
            blueVSI.setARGB(255, 0, 50, 250);
            blueVSI.setAntiAlias(true);

            whiteBorder = new Paint();
            whiteBorder.setColor(Color.WHITE);
            whiteBorder.setStyle(Paint.Style.STROKE);
            whiteBorder.setStrokeWidth(3);
            whiteBorder.setAntiAlias(true);

            whiteThinTics = new Paint();
            whiteThinTics.setColor(Color.WHITE);
            whiteThinTics.setStyle(Paint.Style.FILL);
            whiteThinTics.setStrokeWidth(1);
            whiteThinTics.setAntiAlias(true);

            whiteThickTics = new Paint();
            whiteThickTics.setColor(Color.WHITE);
            whiteThickTics.setStyle(Paint.Style.FILL);
            whiteThickTics.setStrokeWidth(2);
            whiteThickTics.setAntiAlias(true);

            blackSolid = new Paint();
            blackSolid.setColor(Color.BLACK);
            blackSolid.setAntiAlias(true);

            //Pitch's related properties
            pitchScaleWidth = attributes.getDimension(R.styleable.HUD_pitchScaleWidth, 30f);

            //bottom bar properties
            bottomBarHeight = attributes.getDimension(R.styleable.HUD_bottomBarHeight, 30f);

            bottomBarBgPaint = new Paint();
            bottomBarBgPaint.setColor(attributes.getColor(R.styleable.HUD_bottomBarBgColor,
                    Color.BLACK));

            bottomBarVerticalPadding = attributes.getDimension(R.styleable
                    .HUD_bottomBarVerticalPadding, 30f);

            bottomBarHorizontalPadding = attributes.getDimension(R.styleable
                    .HUD_bottomBarHorizontalPadding, 30f);

            //top bar properties
            topBarBgPaint = new Paint();
            topBarBgPaint.setColor(attributes.getColor(R.styleable.HUD_topBarBgColor, Color.BLACK));

            topBarHeight = attributes.getDimension(R.styleable.HUD_topBarHeight, 30f);
        } finally {
            attributes.recycle();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // set center of HUD excluding YAW area
        canvas.translate(width / 2, (height + topBarHeight - bottomBarHeight) / 2);

        // from now on each drawing routine has to undo all applied
        // transformations, clippings, etc by itself
        // this will improve performance because not every routine applies that
        // stuff, so general save and restore is not necessary
        drawPitch(canvas);
        drawInfoText(canvas);
        drawRoll(canvas);
        drawYaw(canvas);
        drawReticle(canvas);
        drawScrollers(canvas);
        drawFailsafe(canvas);
    }

    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        this.width = width;
        this.height = height;

        // do as much precalculation as possible here because it
        // takes some load off the onDraw() routine which is called much more frequently
        updateCommonPaints();
        updateHudText();
        updatePitchVariables();
        updateYawVariables();
        updateRollVariables();
    }

    private void updateCommonPaints() {
        float hudScaleThickTicStrokeWidth;
        float hudScaleThinTicStrokeWidth;
        float hudBorderWidth;
        hudScaleThickTicStrokeWidth = (width + height) / 2
                * HUD_FACTOR_SCALE_THICK_TIC_STROKEWIDTH;
        if (hudScaleThickTicStrokeWidth < 1)
            hudScaleThickTicStrokeWidth = 1;
        whiteThickTics.setStrokeWidth(hudScaleThickTicStrokeWidth);

        hudScaleThinTicStrokeWidth = (width + height) / 2
                * HUD_FACTOR_SCALE_THIN_TIC_STROKEWIDTH;
        if (hudScaleThinTicStrokeWidth < 1)
            hudScaleThinTicStrokeWidth = 1;
        whiteThinTics.setStrokeWidth(hudScaleThinTicStrokeWidth);

        hudBorderWidth = (width + height) / 2 * HUD_FACTOR_BORDER_WIDTH;
        if (hudBorderWidth < 1)
            hudBorderWidth = 1;
        whiteBorder.setStrokeWidth(hudBorderWidth);
    }

    private void updateHudText() {
        attHeightPx = height - (int) topBarHeight - (int) bottomBarHeight;
        final float textSize = textPaint.getTextSize();

        int tempOffset = Math.round(textSize * ATT_FACTOR_INFOTEXT_Y_OFFSET);
        attPosPxInfoTextXOffset = Math.round(width * ATT_FACTOR_INFOTEXT_X_OFFSET);

        int tempAttTextClearance = updateScroller();

        attPosPxInfoTextUpperTop = -attHeightPx / 2 + textSize + tempOffset + tempAttTextClearance;
        attPosPxInfoTextUpperBottom = -attHeightPx / 2 + 2 * textSize + tempOffset + 2 *
                tempAttTextClearance;
        attPosPxInfoTextLowerBottom = attHeightPx / 2 + tempOffset - tempAttTextClearance;
        attPosPxInfoTextLowerTop = attHeightPx / 2 - textSize + tempOffset - 2 *
                tempAttTextClearance;
    }

    private int updateScroller() {
        final float textSize = textPaint.getTextSize();
        int tempAttTextClearance = Math.round(textSize * ATT_FACTOR_INFOTEXT_CLEARANCE);


        scrollerHeightPx = Math.round(attHeightPx * SCROLLER_MAX_HEIGHT_FACTOR);
            tempAttTextClearance = Math.round((attHeightPx - scrollerHeightPx - 4 * textSize) / 6);

        scrollerWidthPx = Math.round(width * SCROLLER_WIDTH_FACTOR);
        scrollerSizePxTextYOffset = Math.round(textSize * SCROLLER_FACTOR_TEXT_Y_OFFSET);
        scrollerSizePxActualTextYOffset = Math.round(textSize * SCROLLER_FACTOR_TEXT_Y_OFFSET);
        scrollerSizePxArrowHeight = Math.round(textSize * SCROLLER_FACTOR_ARROW_HEIGTH);
        scrollerSizePxTextXOffset = Math.round(width * SCROLLER_FACTOR_TEXT_X_OFFSET);
        scrollerSizePxTicLength = Math.round(width * SCROLLER_FACTOR_TIC_LENGTH);

        greenPen.setStrokeWidth(Math.round(attHeightPx * SCROLLER_FACTOR_TARGET_BAR_WIDTH));
        return tempAttTextClearance;
    }

    private void updatePitchVariables() {
        float textSize = textPaint.getTextSize();

        pitchTextCenterOffsetPx = Math.round(-textSize / 2 - textSize * PITCH_FACTOR_TEXT_Y_OFFSET);
        pitchScaleTextXOffset = Math.round(width * PITCH_FACTOR_SCALE_TEXT_X_OFFSET);
        pitchPixPerDegree = Math.round(attHeightPx * PITCH_FACTOR_SCALE_Y_SPACE);
    }

    private void updateRollVariables() {
        rollTopOffsetPx = (int) topBarHeight;
        rollSizePxTics = Math.round(rollTopOffsetPx * ROLL_FACTOR_TIC_LENGTH);
        rollPosPxTextYOffset = Math.round(rollSizePxTics * ROLL_FACTOR_TEXT_Y_OFFSET);
    }

    private void updateYawVariables() {
        int tempOffset;
        yawSizePxTicsSmall = Math.round(topBarHeight * YAW_FACTOR_TICS_SMALL);
        yawSizePxTicsTall = Math.round(topBarHeight * YAW_FACTOR_TICS_TALL);

        float textSize = textPaint.getTextSize();

        tempOffset = Math.round(textSize * YAW_FACTOR_TEXT_Y_OFFSET);
        yawYPosPxText = Math.round(yawSizePxTicsSmall
                + (topBarHeight - yawSizePxTicsSmall) / 2 - textSize / 2 - tempOffset);

        yawYPosPxTextNumbers = Math.round(yawSizePxTicsSmall + (topBarHeight - yawSizePxTicsSmall)
                / 2 - textSize / 2 - tempOffset);
        yawSizePxCenterLineOverRun = Math.round(topBarHeight * YAW_FACTOR_CENTERLINE_OVERRUN);
        yawDegreesPerPixel = width / YAW_DEGREES_TO_SHOW;
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch (event) {
            case ORIENTATION:
                onOrientationUpdate(drone);
                break;

            case SPEED:
                onSpeedAltitudeAndClimbRateUpdate(drone);
                break;

            default:
                updateDroneInfo(drone);
                break;
        }
        invalidate();
        requestLayout();
    }

    private void updateDroneInfo(Drone drone) {
        droneType = drone.type;
        droneState = drone.state;
        droneBattery = drone.battery;
        droneGPS = drone.GPS;
    }

    private void onOrientationUpdate(Drone drone) {
        droneOrientation = drone.orientation;
    }

    private void onSpeedAltitudeAndClimbRateUpdate(Drone drone) {
        droneSpeed = drone.speed;
        droneAltitude = drone.altitude;
    }

    /*
    Private drawing methods
     */
    private void drawInfoText(Canvas canvas) {
        double battVolt = droneBattery.getBattVolt();
        double battCurrent = droneBattery.getBattCurrent();
        double battRemain = droneBattery.getBattRemain();
        double groundSpeed = droneSpeed.getGroundSpeed();
        double airSpeed = droneSpeed.getAirSpeed();
        int satCount = droneGPS.getSatCount();
        String fixType = droneGPS.getFixType();
        String modeName = droneState.getMode().getName();
        double gpsEPH = droneGPS.getGpsEPH();

        final float halfWidth = width / 2;
        final float halfHeight = height / 2;
        final float topInfoBar = halfHeight - bottomBarHeight;

        final float topTextYPos = topInfoBar + bottomBarVerticalPadding;
        final float bottomTextYPos = topTextYPos + bottomBarVerticalPadding;

        //Divide the bottom bar into 4 regions
        final float regionWidth = width / 4;

        //Draw the info bar region
        canvas.drawRect(-width, topInfoBar, width, height, bottomBarBgPaint);
        canvas.drawLine(-width, topInfoBar, width, topInfoBar, whiteBorder);

        // Left Text
        final float leftTextXPos = -halfWidth + bottomBarHorizontalPadding;
        canvas.drawText(String.format("AS %.1fms", airSpeed), leftTextXPos, topTextYPos,
                textPaint);
        canvas.drawText(String.format("GS %.1fms", groundSpeed), leftTextXPos, bottomTextYPos,
                textPaint);

        // Center left Text
        final float centerLeftTextXPos = -regionWidth + bottomBarHorizontalPadding;
        if ((battVolt >= 0) || (battRemain >= 0)) {
            canvas.drawText(String.format("%2.1fV  %.0f%%", battVolt, battRemain),
                    centerLeftTextXPos, topTextYPos, textPaint);
        }
        if (battCurrent >= 0) {
            canvas.drawText(String.format("%2.1fA", battCurrent),
                    centerLeftTextXPos, bottomTextYPos, textPaint);
        }

        // Center right Text
        final float centerRightTextXPos = bottomBarHorizontalPadding;
        canvas.drawText(modeName, centerRightTextXPos, topTextYPos, textPaint);

        // Right Bottom Text
        final float rightTextXPos = regionWidth + bottomBarHorizontalPadding;
        canvas.drawText(fixType, rightTextXPos, topTextYPos, textPaint);
        if (gpsEPH >= 0) {
            canvas.drawText(String.format("hp%.1fm", gpsEPH), rightTextXPos,
                    bottomTextYPos, textPaint);
        }
    }

    private void drawFailsafe(Canvas canvas) {
        int type = droneType.getType();
        boolean isArmed = droneState.isArmed();

        if (ApmModes.isCopter(type)) {
            String armStatus;
            if (isArmed) {
                failsafeTextPaint.setColor(Color.RED);
                armStatus = "ARMED";

            }
            else {
                failsafeTextPaint.setColor(Color.GREEN);
                armStatus = "DISARMED";
            }

            Rect textRec = new Rect();
            failsafeTextPaint.getTextBounds(armStatus, 0, armStatus.length(), textRec);

            textRec.offset(-textRec.width() / 2, canvas.getHeight() / 3);
            RectF boxRec = new RectF(textRec.left
                    - failsafeTextPadding, textRec.top
                    - failsafeTextPadding, textRec.right
                    + failsafeTextPadding, textRec.bottom
                    + failsafeTextPadding);
            canvas.drawRoundRect(boxRec, failsafeTextPadding, failsafeTextPadding,
                    blackSolid);
            canvas.drawText(armStatus, textRec.left - 3, textRec.bottom - 1, failsafeTextPaint);
        }
    }

    private void drawPitch(Canvas canvas) {
        double pitch = droneOrientation.getPitch();
        double roll = droneOrientation.getRoll();

        int pitchOffsetPx = (int) (pitch * pitchPixPerDegree);
        int rollTriangleBottom = -attHeightPx / 2
                + rollTopOffsetPx / 2
                + rollTopOffsetPx;

        canvas.rotate(-(int) roll);


        // Draw the background
        canvas.drawRect(-width, pitchOffsetPx, width, height, groundPaint);
        canvas.drawRect(-width, -height, width, pitchOffsetPx, skyPaint);
        canvas.drawLine(-width, pitchOffsetPx, width, pitchOffsetPx, whiteThinTics);

        // Draw roll triangle
        Path arrow = new Path();
        int tempOffset = Math.round(reticlePaint.getStrokeWidth()
                + whiteBorder.getStrokeWidth() / 2);
        arrow.moveTo(0, -attHeightPx / 2 + rollTopOffsetPx + tempOffset);
        arrow.lineTo(0 - rollTopOffsetPx / 3,
                rollTriangleBottom + tempOffset);
        arrow.lineTo(0 + rollTopOffsetPx / 3,
                rollTriangleBottom + tempOffset);
        arrow.close();
        canvas.drawPath(arrow, reticlePaint);

        // Draw gauge
        int yPos;
        float halfPitchScaleWidth = pitchScaleWidth / 2;
        for (int i = -180; i <= 180; i += 5) {
            yPos = Math.round(-i * pitchPixPerDegree + pitchOffsetPx);
            if ((yPos < -rollTriangleBottom) && (yPos > rollTriangleBottom)
                    && (yPos != pitchOffsetPx)) {
                if (i % 2 == 0) {
                    canvas.drawLine(-pitchScaleWidth, yPos, -pitchScaleTextXOffset,
                            yPos, whiteThinTics);
                    canvas.drawText(String.valueOf(i), 0, yPos - pitchTextCenterOffsetPx,
                            textPaint);
                    canvas.drawLine(pitchScaleTextXOffset, yPos, pitchScaleWidth,
                            yPos, whiteThinTics);
                }
                else
                    canvas.drawLine(-halfPitchScaleWidth, yPos, halfPitchScaleWidth,
                            yPos, whiteThinTics);
            }
        }

        canvas.rotate((int) roll);
    }

    private void drawReticle(Canvas canvas) {
        canvas.drawCircle(0, 0, reticleRadius, reticlePaint);
        canvas.drawLine(-reticleRadius, 0, -reticleRadius * 2, 0, reticlePaint);
        canvas.drawLine(reticleRadius, 0, reticleRadius * 2, 0, reticlePaint);
        canvas.drawLine(0, -reticleRadius, 0, -reticleRadius * 2, reticlePaint);
    }

    private void drawRoll(Canvas canvas) {
        int r = Math.round(attHeightPx / 2 - rollTopOffsetPx);
        RectF rec = new RectF(-r, -r, r, r);

        //Draw the arc
        canvas.drawArc(rec, 225, 90, false, whiteBorder);

        //Draw center triangle
        Path arrow = new Path();
        int tempOffset = Math.round(reticlePaint.getStrokeWidth() / 2);
        arrow.moveTo(0, -attHeightPx / 2 + rollTopOffsetPx - tempOffset);
        arrow.lineTo(-rollTopOffsetPx / 3, -attHeightPx / 2 + rollTopOffsetPx / 2 -
                tempOffset);
        arrow.lineTo(rollTopOffsetPx / 3, -attHeightPx / 2 + rollTopOffsetPx / 2 -
                tempOffset);
        arrow.close();
        canvas.drawPath(arrow, reticlePaint);

        //Draw the ticks
        //The center of the circle is at: 0, 0
        for (int i = -45; i <= 45; i += 15) {
            if (i != 0) {
                //Draw ticks
                float dx = (float) Math.sin(i * Math.PI / 180) * r;
                float dy = (float) Math.cos(i * Math.PI / 180) * r;
                float ex = (float) Math.sin(i * Math.PI / 180) * (r + rollSizePxTics);
                float ey = (float) Math.cos(i * Math.PI / 180) * (r + rollSizePxTics);

                canvas.drawLine(dx, -dy, ex, -ey, whiteThickTics);

                //Draw the labels
//                dx = (float) Math.sin(i * Math.PI / 180) * (r + rollSizePxTics +
//                        rollPosPxTextYOffset);
//                dy = (float) Math.cos(i * Math.PI / 180) * (r + rollSizePxTics +
//                        rollPosPxTextYOffset);
//                canvas.drawText(Math.abs(i) + "", dx, -dy, textPaint);
            }
        }

        //current roll angle will be drawn by drawPitch()
    }

    private void drawScrollers(Canvas canvas) {
        //Drawing left scroller
        double groundSpeed = droneSpeed.getGroundSpeed();
        double airSpeed = droneSpeed.getAirSpeed();
        double targetSpeed = droneSpeed.getTargetSpeed();

        final float textHalfSize = textPaint.getTextSize() / 2;

        double speed = airSpeed;
        if (speed == 0)
            speed = groundSpeed;

        // Outside box
        RectF scroller = new RectF(-width / 2, -scrollerHeightPx / 2,
                -width / 2 + scrollerWidthPx, scrollerHeightPx / 2);

        // Draw Scroll
        canvas.drawRect(scroller, scrollerBgPaint);
        canvas.drawRect(scroller, whiteBorder);
        // Clip to Scroller
        canvas.clipRect(scroller, Region.Op.REPLACE);

        float space = scroller.height()
                / (float) SCROLLER_SPEED_RANGE;
        int start = ((int) speed - SCROLLER_SPEED_RANGE / 2);

        if (start > targetSpeed) {
            canvas.drawLine(scroller.left, scroller.bottom, scroller.right,
                    scroller.bottom, greenPen);
        }
        else if ((speed + SCROLLER_SPEED_RANGE / 2) < targetSpeed) {
            canvas.drawLine(scroller.left, scroller.top, scroller.right,
                    scroller.top, greenPen);
        }

        float targetSpdPos = Float.MIN_VALUE;
        for (int a = start; a <= (speed + SCROLLER_SPEED_RANGE / 2); a += 1) {
            float lineHeight = scroller.centerY() - space * (a - (int) speed);

            if (a == ((int) targetSpeed) && targetSpeed != 0) {
                canvas.drawLine(scroller.left, lineHeight, scroller.right,
                        lineHeight, greenPen);
                targetSpdPos = lineHeight;
            }
            if (a % 5 == 0) {
                canvas.drawLine(scroller.right, lineHeight, scroller.right
                        - scrollerSizePxTicLength, lineHeight,
                        whiteThickTics);
                canvas.drawText(Integer.toString(a), scroller.right
                        - scrollerSizePxTextXOffset, lineHeight + textHalfSize
                        + scrollerSizePxTextYOffset, textPaint);
            }
        }

        // Arrow with current speed
        String actualText = Integer.toString((int) speed);
        int borderWidth = Math.round(whiteBorder
                .getStrokeWidth());
        Path arrow = new Path();
        arrow.moveTo(scroller.left - borderWidth,
                -scrollerSizePxArrowHeight / 2);
        arrow.lineTo(scroller.right - scrollerSizePxArrowHeight / 4
                - borderWidth, -scrollerSizePxArrowHeight / 2);
        arrow.lineTo(scroller.right - borderWidth, 0);
        arrow.lineTo(scroller.right - scrollerSizePxArrowHeight / 4
                - borderWidth, scrollerSizePxArrowHeight / 2);
        arrow.lineTo(scroller.left - borderWidth, scrollerSizePxArrowHeight / 2);
        canvas.drawPath(arrow, blackSolid);
        if ((targetSpdPos != Float.MIN_VALUE)
                && (targetSpdPos > -scrollerSizePxArrowHeight / 2)
                && (targetSpdPos < scrollerSizePxArrowHeight / 2)) {
            Rect actualTextRec = new Rect();
            textPaint.getTextBounds(actualText, 0,
                    actualText.length(), actualTextRec);
            canvas.drawLine(scroller.left, targetSpdPos, scroller.right
                    - actualTextRec.width() - scrollerSizePxTextXOffset
                    - textHalfSize, targetSpdPos, greenPen);
        }
        canvas.drawPath(arrow, reticlePaint);
        canvas.drawText(actualText, scroller.right - scrollerSizePxTextXOffset,
                textPaint.getTextSize() / 2
                        + scrollerSizePxActualTextYOffset, textPaint);
        // Reset clipping of Scroller
        canvas.clipRect(-width / 2, -height / 2,
                width / 2, height / 2, Region.Op.REPLACE);

        /* Drawing right scroller */
        double altitude = droneAltitude.getAltitude();
        double targetAltitude = droneAltitude.getTargetAltitude();
        double verticalSpeed = droneSpeed.getVerticalSpeed();

        // Outside box
        scroller = new RectF(width / 2 - scrollerWidthPx,
                -scrollerHeightPx / 2, width / 2, scrollerHeightPx / 2);

        // Draw Vertical speed indicator
        final float vsi_width = scroller.width() / 4;
        float linespace = scroller.height() / SCROLLER_VSI_RANGE;
        Path vsiBox = new Path();
        vsiBox.moveTo(scroller.left, scroller.top); // draw outside box
        vsiBox.lineTo(scroller.left - vsi_width, scroller.top + vsi_width);
        vsiBox.lineTo(scroller.left - vsi_width, scroller.bottom - vsi_width);
        vsiBox.lineTo(scroller.left, scroller.bottom);
        Path vsiFill = new Path();
        float vsiIndicatorEnd = scroller.centerY() - ((float) verticalSpeed)
                * linespace;
        vsiFill.moveTo(scroller.left, scroller.centerY());
        vsiFill.lineTo(scroller.left - vsi_width, scroller.centerY());
        vsiFill.lineTo(scroller.left - vsi_width, vsiIndicatorEnd);
        vsiFill.lineTo(scroller.left, vsiIndicatorEnd);
        vsiFill.lineTo(scroller.left, scroller.centerY());
        canvas.drawPath(vsiBox, scrollerBgPaint);
        canvas.drawPath(vsiFill, blueVSI);
        canvas.drawLine(scroller.left - vsi_width, vsiIndicatorEnd, scroller.left, vsiIndicatorEnd,
                whiteThinTics);
        canvas.drawPath(vsiBox, whiteBorder);

        for (int a = 1; a < SCROLLER_VSI_RANGE; a++) { // draw ticks
            float lineHeight = scroller.top + linespace * a;
            canvas.drawLine(scroller.left - vsi_width, lineHeight, scroller.left - vsi_width +
                    vsi_width / 3, lineHeight, whiteThickTics);
        }

        // Draw Altitude Scroller
        canvas.drawRect(scroller, scrollerBgPaint);
        canvas.drawRect(scroller, whiteBorder);

        // Clip to Scroller
        canvas.clipRect(scroller, Region.Op.REPLACE);

        space = scroller.height() / (float) SCROLLER_ALT_RANGE;
        start = ((int) altitude - SCROLLER_ALT_RANGE / 2);

        if (start > targetAltitude) {
            canvas.drawLine(scroller.left, scroller.bottom, scroller.right, scroller.bottom,
                    greenPen);
        }
        else if ((altitude + SCROLLER_SPEED_RANGE / 2) < targetAltitude) {
            canvas.drawLine(scroller.left, scroller.top, scroller.right,
                    scroller.top, greenPen);
        }

        float targetAltPos = Float.MIN_VALUE;
        for (int a = start; a <= (altitude + SCROLLER_ALT_RANGE / 2); a += 1) { // go
            // trough 1m steps

            float lineHeight = scroller.centerY() - space * (a - (int) altitude);

            if (a == ((int) targetAltitude) && targetAltitude != 0) {
                canvas.drawLine(scroller.left, lineHeight, scroller.right,
                        lineHeight, greenPen);
                targetAltPos = lineHeight;
            }
            if (a % 5 == 0) {
                canvas.drawLine(scroller.left, lineHeight, scroller.left
                        + scrollerSizePxTicLength, lineHeight,
                        whiteThickTics);
                canvas.drawText(Integer.toString(a), scroller.left
                        + scrollerSizePxTextXOffset, lineHeight + textHalfSize
                        + scrollerSizePxTextYOffset, textPaint);
            }
        }

        // Arrow with current altitude
        actualText = Integer.toString((int) altitude);
        borderWidth = Math.round(whiteBorder.getStrokeWidth());
        arrow = new Path();
        arrow.moveTo(scroller.right, -scrollerSizePxArrowHeight / 2);
        arrow.lineTo(scroller.left + scrollerSizePxArrowHeight / 4
                + borderWidth, -scrollerSizePxArrowHeight / 2);
        arrow.lineTo(scroller.left + borderWidth, 0);
        arrow.lineTo(scroller.left + scrollerSizePxArrowHeight / 4
                + borderWidth, scrollerSizePxArrowHeight / 2);
        arrow.lineTo(scroller.right, scrollerSizePxArrowHeight / 2);
        canvas.drawPath(arrow, blackSolid);
        if ((targetAltPos != Float.MIN_VALUE)
                && (targetAltPos > -scrollerSizePxArrowHeight / 2)
                && (targetAltPos < scrollerSizePxArrowHeight / 2)) {
            Rect actualTextRec = new Rect();
            textPaint.getTextBounds(actualText, 0, actualText.length(), actualTextRec);
            canvas.drawLine(scroller.right, targetAltPos, scroller.left
                    + actualTextRec.width() + scrollerSizePxTextXOffset
                    + textHalfSize, targetAltPos, greenPen);
        }
        canvas.drawPath(arrow, reticlePaint);
        canvas.drawText(actualText, scroller.left + scrollerSizePxTextXOffset,
                textPaint.getTextSize() / 2
                        + scrollerSizePxActualTextYOffset, textPaint);
        // Reset clipping of Scroller
        canvas.clipRect(-width / 2, -height / 2,
                width / 2, height / 2, Region.Op.REPLACE);
        // Draw VSI center indicator
        canvas.drawLine(scroller.left + borderWidth, 0, scroller.left
                - vsi_width - borderWidth, 0, reticlePaint);
    }

    private void drawYaw(Canvas canvas) {
        double yaw = droneOrientation.getYaw();

        int yawBottom = -attHeightPx / 2;
        canvas.drawRect(-width / 2, yawBottom - topBarHeight, width / 2, yawBottom, topBarBgPaint);
        canvas.drawLine(-width / 2, yawBottom, width / 2, yawBottom, whiteBorder);

        double centerDegrees = yaw;

        double mod = yaw % 5;
        for (double angle = (centerDegrees - mod) - YAW_DEGREES_TO_SHOW / 2.0;
             angle <= (centerDegrees - mod) + YAW_DEGREES_TO_SHOW / 2.0; angle += 5) {

            // protect from wraparound
            double workAngle = (angle + 360.0);
            while (workAngle >= 360)
                workAngle -= 360.0;

            // need to draw "angle"
            // How many pixels from center should it be?
            int distanceToCenter = (int) ((angle - centerDegrees) * yawDegreesPerPixel);

            if (workAngle % 45 == 0) {
                String compass[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
                int index = (int) workAngle / 45;
                canvas.drawLine(distanceToCenter, yawBottom
                        - yawSizePxTicsSmall, distanceToCenter, yawBottom,
                        whiteThinTics);
                canvas.drawText(compass[index], distanceToCenter, yawBottom
                        - yawYPosPxText, textPaint);
            }
            else if (workAngle % 15 == 0) {
                canvas.drawLine(distanceToCenter,
                        yawBottom - yawSizePxTicsTall, distanceToCenter,
                        yawBottom, whiteThinTics);
                canvas.drawText((int) (workAngle) + "", distanceToCenter,
                        yawBottom - yawYPosPxTextNumbers, textPaint);
            }
            else {
                canvas.drawLine(distanceToCenter, yawBottom
                        - yawSizePxTicsSmall, distanceToCenter, yawBottom,
                        whiteThinTics);
            }
        }

        // Draw the center line
        canvas.drawLine(0, yawBottom - topBarHeight, 0, yawBottom + yawSizePxCenterLineOverRun,
                reticlePaint);
    }

    /*
    Properties getters, and setters
     */
    public int getGroundColor() {
        return groundPaint.getColor();
    }

    public void setGroundColor(int color) {
        groundPaint.setColor(color);
        invalidate();
        requestLayout();
    }

    public int getSkyColor() {
        return skyPaint.getColor();
    }

    public void setSkyColor(int color) {
        skyPaint.setColor(color);
        invalidate();
        requestLayout();
    }

    public int getReticleColor() {
        return reticlePaint.getColor();
    }

    public void setReticleColor(int color) {
        reticlePaint.setColor(color);
        invalidate();
        requestLayout();
    }

    public float getReticleRadius() {
        return reticleRadius;
    }

    public void setReticleRadius(float radius) {
        reticleRadius = radius;
        invalidate();
        requestLayout();
    }

    public int getTextColor() {
        return textPaint.getColor();
    }

    public void setTextColor(int color) {
        textPaint.setColor(color);
        invalidate();
        requestLayout();
    }

    public float getTextSize() {
        return textPaint.getTextSize();
    }

    public void setTextSize(float textSize) {
        textPaint.setTextSize(textSize);
        invalidate();
        requestLayout();
    }

    public int getYawBgColor() {
        return topBarBgPaint.getColor();
    }

    public void setYawBgColor(int color) {
        topBarBgPaint.setColor(color);
        invalidate();
        requestLayout();
    }

}
