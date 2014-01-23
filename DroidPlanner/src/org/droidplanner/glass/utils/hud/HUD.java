package org.droidplanner.glass.utils.hud;

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
import com.MAVLink.Messages.enums.MAV_TYPE;
import org.droidplanner.R;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces;
import org.droidplanner.drone.DroneInterfaces.OnDroneListner;
import org.droidplanner.drone.variables.Altitude;
import org.droidplanner.drone.variables.Battery;
import org.droidplanner.drone.variables.GPS;
import org.droidplanner.drone.variables.Orientation;
import org.droidplanner.drone.variables.Speed;
import org.droidplanner.drone.variables.State;
import org.droidplanner.drone.variables.Type;

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
public class HUD extends View {

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

    private int width;
    private int height;
    private boolean enabled;

    /**
     * Paint used to draw the ground when the hud is disabled.
     */
    private Paint disabledGroundPaint;

    /**
     * Paint used to draw the sky when the hud is disabled.
     */
    private Paint disabledSkyPaint;

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
    private float scrollerHeight;
    private float scrollerWidth;
    private float scrollerArrowHeight;
    private float scrollerTicWidth;
    private float scrollerTextHorizontalMargin;
    private float scrollerTextVerticalMargin;

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

    /*
    Common paint's variables
     */
    public Paint whiteBorder;
    public Paint whiteThickTics;
    public Paint whiteThinTics;
    public Paint blackSolid;

    /*
    Common variables, used to avoid unnecessary allocation within draw's related calls.
     */
    private final Path commonPath = new Path();
    private final Rect commonRect = new Rect();
    private final RectF commonRectFloat = new RectF();

    /*
    HUD's properties
     */
    private double altitude;
    private double targetAltitude;

    private double verticalSpeed;
    private double groundSpeed;
    private double airSpeed;
    private double targetSpeed;

    private double pitch;
    private double roll;
    private double yaw;

    private int droneType = MAV_TYPE.MAV_TYPE_FIXED_WING;

    private double battVolt = -1;
    private double battRemain = -1;
    private double battCurrent = -1;

    private boolean isDroneArmed;
    private ApmModes flightMode = ApmModes.UNKNOWN;

    private double gps_eph = -1;
    private int satCount = -1;
    private int fixType = -1;

    public HUD(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public HUD(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.HUD,
                defStyle, 0);

        try {
            enabled = attributes.getBoolean(R.styleable.HUD_android_enabled, false);

            disabledGroundPaint = new Paint();
            disabledGroundPaint.setColor(Color.DKGRAY);

            disabledSkyPaint = new Paint();
            disabledSkyPaint.setColor(Color.LTGRAY);

            groundPaint = new Paint();
            groundPaint.setColor(attributes.getColor(R.styleable.HUD_groundColor, Color.argb(220,
                    148, 193, 31)));

            skyPaint = new Paint();
            skyPaint.setColor(attributes.getColor(R.styleable.HUD_skyColor, Color.argb(220, 0,
                    113, 188)));

            textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(attributes.getColor(R.styleable.HUD_android_textColor, Color.WHITE));
            textPaint.setTextSize(attributes.getDimension(R.styleable.HUD_android_textSize, 25f));

            reticlePaint = new Paint();
            reticlePaint.setStyle(Paint.Style.STROKE);
            reticlePaint.setStrokeWidth(3);
            reticlePaint.setAntiAlias(true);
            reticlePaint.setColor(attributes.getColor(R.styleable.HUD_reticleColor, Color.RED));

            reticleRadius = attributes.getDimension(R.styleable.HUD_reticleRadius, 10f);

            failsafeTextPaint = new Paint();
            failsafeTextPaint.setAntiAlias(true);
            failsafeTextPaint.setTextSize(attributes.getDimension(R.styleable
                    .HUD_failsafeTextSize, 37f));

            failsafeTextPadding = attributes.getDimension(R.styleable.HUD_failsafeTextPadding,
                    25f);

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

            //top bar properties
            topBarBgPaint = new Paint();
            topBarBgPaint.setColor(attributes.getColor(R.styleable.HUD_topBarBgColor, Color.BLACK));

            topBarHeight = attributes.getDimension(R.styleable.HUD_topBarHeight, 30f);

            //Scroller properties
            scrollerHeight = attributes.getDimension(R.styleable.HUD_scrollerHeight, 200f);
            scrollerWidth = attributes.getDimension(R.styleable.HUD_scrollerWidth, 96f);
            scrollerBgPaint = new Paint();
            scrollerBgPaint.setColor(attributes.getColor(R.styleable.HUD_scrollerBgColor,
                    Color.argb(64, 255, 255, 255)));
            scrollerArrowHeight = attributes.getDimension(R.styleable.HUD_scrollerArrowHeight,
                    25f);
            scrollerTicWidth = attributes.getDimension(R.styleable.HUD_scrollerTicWidth, 16f);
            scrollerTextHorizontalMargin = attributes.getDimension(R.styleable.HUD_scrollerTextHorizontalMargin, 23f);
            scrollerTextVerticalMargin = attributes.getDimension(R.styleable
                    .HUD_scrollerTextVerticalMargin, 10f);
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

    private Paint getGroundPaint(){
        if(isEnabled()){
            return groundPaint;
        }
        else{
            return disabledGroundPaint;
        }
    }

    private Paint getSkyPaint(){
        if(isEnabled()){
            return skyPaint;
        }
        else{
            return disabledSkyPaint;
        }
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

        int tempAttTextClearance = Math.round((attHeightPx - scrollerHeight - 4 * textSize) / 6);

        attPosPxInfoTextUpperTop = -attHeightPx / 2 + textSize + tempOffset + tempAttTextClearance;
        attPosPxInfoTextUpperBottom = -attHeightPx / 2 + 2 * textSize + tempOffset + 2 *
                tempAttTextClearance;
        attPosPxInfoTextLowerBottom = attHeightPx / 2 + tempOffset - tempAttTextClearance;
        attPosPxInfoTextLowerTop = attHeightPx / 2 - textSize + tempOffset - 2 *
                tempAttTextClearance;
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

    /*
    Private drawing methods
     */
    private void drawInfoText(Canvas canvas) {
        String fixType = getFixType();

        final float halfWidth = width / 2;
        final float halfHeight = height / 2;
        final float topInfoBar = halfHeight - bottomBarHeight;

        final float topTextYPos = topInfoBar + bottomBarVerticalPadding;
        final float bottomTextYPos = topTextYPos + bottomBarVerticalPadding;

        //Divide the bottom bar into 3 regions
        final float regionWidth = width / 3.5f;

        //Draw the info bar region
        canvas.drawRect(-width, topInfoBar, width, height, bottomBarBgPaint);
        canvas.drawLine(-width, topInfoBar, width, topInfoBar, whiteBorder);

        if (isEnabled()) {
            // Left Text
            final float leftTextXPos = -regionWidth;
            canvas.drawText(String.format("AS %.1fms", airSpeed), leftTextXPos, topTextYPos,
                    textPaint);
            canvas.drawText(String.format("GS %.1fms", groundSpeed), leftTextXPos, bottomTextYPos,
                    textPaint);

            // Center Text
            final float centerLeftTextXPos = 0;
            if ((battVolt >= 0) || (battRemain >= 0)) {
                canvas.drawText(String.format("%2.1fV  %.0f%%", battVolt, battRemain),
                        centerLeftTextXPos, topTextYPos, textPaint);
            }
            if (battCurrent >= 0) {
                canvas.drawText(String.format("%2.1fA", battCurrent),
                        centerLeftTextXPos, bottomTextYPos, textPaint);
            }

            // Right Text
            final float rightTextXPos = regionWidth;
            canvas.drawText(flightMode.getName(), rightTextXPos, topTextYPos, textPaint);

            String satInfo = fixType;
            if (gps_eph >= 0) {
                satInfo += String.format(" ( hp%.1fm )", gps_eph);
            }
            canvas.drawText(satInfo, rightTextXPos, bottomTextYPos, textPaint);
        }
    }

    private void drawFailsafe(Canvas canvas) {
        if (!isEnabled())
            return;

        if (ApmModes.isCopter(droneType)) {
            String armStatus;
            if (isDroneArmed) {
                failsafeTextPaint.setColor(Color.RED);
                armStatus = "ARMED";

            }
            else {
                failsafeTextPaint.setColor(Color.GREEN);
                armStatus = "DISARMED";
            }

            commonRect.set(0, 0, 0, 0);
            failsafeTextPaint.getTextBounds(armStatus, 0, armStatus.length(), commonRect);

            commonRect.offset(-commonRect.width() / 2, canvas.getHeight() / 3);
            commonRectFloat.set(commonRect.left - failsafeTextPadding,
                    commonRect.top - failsafeTextPadding,
                    commonRect.right + failsafeTextPadding,
                    commonRect.bottom + failsafeTextPadding);
            canvas.drawRoundRect(commonRectFloat, failsafeTextPadding, failsafeTextPadding,
                    blackSolid);
            canvas.drawText(armStatus, commonRect.left - 3, commonRect.bottom - 1,
                    failsafeTextPaint);
        }
    }

    private void drawPitch(Canvas canvas) {
        int pitchOffsetPx = (int) (pitch * pitchPixPerDegree);
        int rollTriangleBottom = -attHeightPx / 2
                + rollTopOffsetPx / 2
                + rollTopOffsetPx;

        canvas.rotate(-(int) roll);


        // Draw the background
        canvas.drawRect(-width, pitchOffsetPx, width, height, getGroundPaint());
        canvas.drawRect(-width, -height, width, pitchOffsetPx, getSkyPaint());
        canvas.drawLine(-width, pitchOffsetPx, width, pitchOffsetPx, whiteThinTics);

        // Draw roll triangle
        commonPath.reset();
        Path arrow = commonPath;
        int tempOffset = Math.round(reticlePaint.getStrokeWidth() + whiteBorder.getStrokeWidth()
                / 2);
        arrow.moveTo(0, -attHeightPx / 2 + rollTopOffsetPx + tempOffset);
        arrow.lineTo(0 - rollTopOffsetPx / 3, rollTriangleBottom + tempOffset);
        arrow.lineTo(0 + rollTopOffsetPx / 3, rollTriangleBottom + tempOffset);
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
        commonRectFloat.set(-r, -r, r, r);

        //Draw the arc
        canvas.drawArc(commonRectFloat, 225, 90, false, whiteBorder);

        //Draw center triangle
        commonPath.reset();
        Path arrow = commonPath;
        int tempOffset = Math.round(reticlePaint.getStrokeWidth() / 2);
        arrow.moveTo(0, -attHeightPx / 2 + rollTopOffsetPx - tempOffset);
        arrow.lineTo(-rollTopOffsetPx / 3, -attHeightPx / 2 + rollTopOffsetPx / 2 - tempOffset);
        arrow.lineTo(rollTopOffsetPx / 3, -attHeightPx / 2 + rollTopOffsetPx / 2 - tempOffset);
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
        final float textHalfSize = textPaint.getTextSize() / 2;

        double speed = airSpeed;
        if (speed == 0)
            speed = groundSpeed;

        // Outside box
        commonRectFloat.set(-width / 2, -scrollerHeight / 2, -width / 2 + scrollerWidth,
                scrollerHeight / 2);

        // Draw Scroll
        canvas.drawRect(commonRectFloat, scrollerBgPaint);
        canvas.drawRect(commonRectFloat, whiteBorder);

        // Clip to Scroller
        canvas.clipRect(commonRectFloat, Region.Op.REPLACE);

        float space = commonRectFloat.height() / (float) SCROLLER_SPEED_RANGE;
        int start = ((int) speed - SCROLLER_SPEED_RANGE / 2);

        if (start > targetSpeed) {
            canvas.drawLine(commonRectFloat.left, commonRectFloat.bottom, commonRectFloat.right,
                    commonRectFloat.bottom, greenPen);
        }
        else if ((speed + SCROLLER_SPEED_RANGE / 2) < targetSpeed) {
            canvas.drawLine(commonRectFloat.left, commonRectFloat.top, commonRectFloat.right,
                    commonRectFloat.top, greenPen);
        }

        float targetSpdPos = Float.MIN_VALUE;
        for (int a = start; a <= (speed + SCROLLER_SPEED_RANGE / 2); a += 1) {
            float lineHeight = commonRectFloat.centerY() - space * (a - (int) speed);

            if (a == ((int) targetSpeed) && targetSpeed != 0) {
                canvas.drawLine(commonRectFloat.left, lineHeight, commonRectFloat.right,
                        lineHeight, greenPen);
                targetSpdPos = lineHeight;
            }
            if (a % 5 == 0) {
                canvas.drawLine(commonRectFloat.right, lineHeight, commonRectFloat.right
                        - scrollerTicWidth, lineHeight, whiteThickTics);
                canvas.drawText(Integer.toString(a), commonRectFloat.right
                        - scrollerTextHorizontalMargin, lineHeight + textHalfSize
                        - scrollerTextVerticalMargin, textPaint);
            }
        }

        // Arrow with current speed
        String actualText = Integer.toString((int) speed);
        int borderWidth = Math.round(whiteBorder.getStrokeWidth());

        commonPath.reset();
        Path arrow = commonPath;
        arrow.moveTo(commonRectFloat.left - borderWidth, -scrollerArrowHeight / 2);
        arrow.lineTo(commonRectFloat.right - scrollerArrowHeight / 4
                - borderWidth, -scrollerArrowHeight / 2);
        arrow.lineTo(commonRectFloat.right - borderWidth, 0);
        arrow.lineTo(commonRectFloat.right - scrollerArrowHeight / 4
                - borderWidth, scrollerArrowHeight / 2);
        arrow.lineTo(commonRectFloat.left - borderWidth, scrollerArrowHeight / 2);
        canvas.drawPath(arrow, blackSolid);

        if ((targetSpdPos != Float.MIN_VALUE)
                && (targetSpdPos > -scrollerArrowHeight / 2)
                && (targetSpdPos < scrollerArrowHeight / 2)) {
            commonRect.set(0, 0, 0, 0);
            textPaint.getTextBounds(actualText, 0, actualText.length(), commonRect);
            canvas.drawLine(commonRectFloat.left, targetSpdPos,
                    commonRectFloat.right - commonRect.width() - scrollerTextHorizontalMargin
                            - textHalfSize, targetSpdPos, greenPen);
        }

        canvas.drawPath(arrow, reticlePaint);
        canvas.drawText(actualText, commonRectFloat.right - scrollerTextHorizontalMargin,
                textPaint.getTextSize() / 2 - scrollerTextVerticalMargin, textPaint);

        // Reset clipping of Scroller
        canvas.clipRect(-width / 2, -height / 2,
                width / 2, height / 2, Region.Op.REPLACE);

        /* Drawing right scroller */
        // Outside box
        commonRectFloat.set(width / 2 - scrollerWidth, -scrollerHeight / 2, width / 2,
                scrollerHeight / 2);

        // Draw Vertical speed indicator
        final float vsi_width = commonRectFloat.width() / 4;
        float linespace = commonRectFloat.height() / SCROLLER_VSI_RANGE;

        commonPath.reset();
        Path vsiBox = commonPath;
        vsiBox.moveTo(commonRectFloat.left, commonRectFloat.top); // draw outside box
        vsiBox.lineTo(commonRectFloat.left - vsi_width, commonRectFloat.top + vsi_width);
        vsiBox.lineTo(commonRectFloat.left - vsi_width, commonRectFloat.bottom - vsi_width);
        vsiBox.lineTo(commonRectFloat.left, commonRectFloat.bottom);
        canvas.drawPath(vsiBox, scrollerBgPaint);
        canvas.drawPath(vsiBox, whiteBorder);

        commonPath.reset();
        Path vsiFill = commonPath;
        float vsiIndicatorEnd = commonRectFloat.centerY() - ((float) verticalSpeed) * linespace;
        vsiFill.moveTo(commonRectFloat.left, commonRectFloat.centerY());
        vsiFill.lineTo(commonRectFloat.left - vsi_width, commonRectFloat.centerY());
        vsiFill.lineTo(commonRectFloat.left - vsi_width, vsiIndicatorEnd);
        vsiFill.lineTo(commonRectFloat.left, vsiIndicatorEnd);
        vsiFill.lineTo(commonRectFloat.left, commonRectFloat.centerY());
        canvas.drawPath(vsiFill, blueVSI);

        canvas.drawLine(commonRectFloat.left - vsi_width, vsiIndicatorEnd, commonRectFloat.left,
                vsiIndicatorEnd, whiteThinTics);

        for (int a = 1; a < SCROLLER_VSI_RANGE; a++) { // draw ticks
            float lineHeight = commonRectFloat.top + linespace * a;
            canvas.drawLine(commonRectFloat.left - vsi_width, lineHeight,
                    commonRectFloat.left - vsi_width + vsi_width / 3, lineHeight, whiteThickTics);
        }

        // Draw Altitude Scroller
        canvas.drawRect(commonRectFloat, scrollerBgPaint);
        canvas.drawRect(commonRectFloat, whiteBorder);

        // Clip to Scroller
        canvas.clipRect(commonRectFloat, Region.Op.REPLACE);

        space = commonRectFloat.height() / (float) SCROLLER_ALT_RANGE;
        start = ((int) altitude - SCROLLER_ALT_RANGE / 2);

        if (start > targetAltitude) {
            canvas.drawLine(commonRectFloat.left, commonRectFloat.bottom, commonRectFloat.right,
                    commonRectFloat.bottom, greenPen);
        }
        else if ((altitude + SCROLLER_SPEED_RANGE / 2) < targetAltitude) {
            canvas.drawLine(commonRectFloat.left, commonRectFloat.top, commonRectFloat.right,
                    commonRectFloat.top, greenPen);
        }

        float targetAltPos = Float.MIN_VALUE;
        for (int a = start; a <= (altitude + SCROLLER_ALT_RANGE / 2); a += 1) { // go
            // trough 1m steps
            float lineHeight = commonRectFloat.centerY() - space * (a - (int) altitude);

            if (a == ((int) targetAltitude) && targetAltitude != 0) {
                canvas.drawLine(commonRectFloat.left, lineHeight, commonRectFloat.right,
                        lineHeight, greenPen);
                targetAltPos = lineHeight;
            }
            if (a % 5 == 0) {
                canvas.drawLine(commonRectFloat.left, lineHeight, commonRectFloat.left
                        + scrollerTicWidth, lineHeight, whiteThickTics);
                canvas.drawText(Integer.toString(a), commonRectFloat.left
                        + scrollerTextHorizontalMargin, lineHeight + textHalfSize
                        - scrollerTextVerticalMargin, textPaint);
            }
        }

        // Arrow with current altitude
        actualText = Integer.toString((int) altitude);
        borderWidth = Math.round(whiteBorder.getStrokeWidth());

        commonPath.reset();
        arrow = commonPath;
        arrow.moveTo(commonRectFloat.right, -scrollerArrowHeight / 2);
        arrow.lineTo(commonRectFloat.left + scrollerArrowHeight / 4
                + borderWidth, -scrollerArrowHeight / 2);
        arrow.lineTo(commonRectFloat.left + borderWidth, 0);
        arrow.lineTo(commonRectFloat.left + scrollerArrowHeight / 4
                + borderWidth, scrollerArrowHeight / 2);
        arrow.lineTo(commonRectFloat.right, scrollerArrowHeight / 2);
        canvas.drawPath(arrow, blackSolid);

        if ((targetAltPos != Float.MIN_VALUE)
                && (targetAltPos > -scrollerArrowHeight / 2)
                && (targetAltPos < scrollerArrowHeight / 2)) {
            commonRect.set(0, 0, 0, 0);
            textPaint.getTextBounds(actualText, 0, actualText.length(), commonRect);
            canvas.drawLine(commonRectFloat.right, targetAltPos, commonRectFloat.left
                    + commonRect.width() + scrollerTextHorizontalMargin
                    + textHalfSize, targetAltPos, greenPen);
        }
        canvas.drawPath(arrow, reticlePaint);
        canvas.drawText(actualText, commonRectFloat.left + scrollerTextHorizontalMargin,
                textPaint.getTextSize() / 2 - scrollerTextVerticalMargin, textPaint);

        // Reset clipping of Scroller
        canvas.clipRect(-width / 2, -height / 2, width / 2, height / 2, Region.Op.REPLACE);

        // Draw VSI center indicator
        canvas.drawLine(commonRectFloat.left + borderWidth, 0, commonRectFloat.left
                - vsi_width - borderWidth, 0, reticlePaint);
    }

    private void drawYaw(Canvas canvas) {
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
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        invalidate();
    }

    public void updateGpsInfo(GPS gps){
        this.gps_eph = gps.getGpsEPH();
        this.fixType = gps.getFixTypeNumeric();
        this.satCount = gps.getSatCount();
        invalidate();
    }

    private String getFixType() {
        String gpsFix;
        switch (fixType) {
            case 2:
                gpsFix = ("2D");
                break;
            case 3:
                gpsFix = ("3D");
                break;
            default:
                gpsFix = ("NoFix");
                break;
        }
        return gpsFix;
    }

    public void updateDroneState(State state){
        this.isDroneArmed = state.isArmed();
        this.flightMode = state.getMode();
        invalidate();
    }

    public void updateBatteryInfo(Battery droneBattery){
        this.battCurrent = droneBattery.getBattCurrent();
        this.battRemain = droneBattery.getBattRemain();
        this.battVolt = droneBattery.getBattVolt();
        invalidate();
    }


    public int getDroneType() {
        return droneType;
    }

    public void setDroneType(int droneType) {
        this.droneType = droneType;
        invalidate();
    }

    public void updateOrientation(Orientation orientation){
        this.pitch = orientation.getPitch();
        this.roll = orientation.getRoll();
        this.yaw = orientation.getYaw();
        invalidate();
    }

    public void updateAltitudeAndSpeed(Altitude droneAltitude, Speed droneSpeed){
        this.altitude = droneAltitude.getAltitude();
        this.targetAltitude = droneAltitude.getTargetAltitude();

        this.verticalSpeed = droneSpeed.getVerticalSpeed();
        this.airSpeed = droneSpeed.getAirSpeed();
        this.groundSpeed = droneSpeed.getGroundSpeed();
        this.targetSpeed = droneSpeed.getTargetSpeed();

        invalidate();
    }

    public int getGroundColor() {
        return groundPaint.getColor();
    }

    public void setGroundColor(int color) {
        groundPaint.setColor(color);
        invalidate();
    }

    public int getSkyColor() {
        return skyPaint.getColor();
    }

    public void setSkyColor(int color) {
        skyPaint.setColor(color);
        invalidate();
    }

    public int getReticleColor() {
        return reticlePaint.getColor();
    }

    public void setReticleColor(int color) {
        reticlePaint.setColor(color);
        invalidate();
    }

    public float getReticleRadius() {
        return reticleRadius;
    }

    public void setReticleRadius(float radius) {
        reticleRadius = radius;
        invalidate();
    }

    public int getTextColor() {
        return textPaint.getColor();
    }

    public void setTextColor(int color) {
        textPaint.setColor(color);
        invalidate();
    }

    public float getTextSize() {
        return textPaint.getTextSize();
    }

    public void setTextSize(float textSize) {
        textPaint.setTextSize(textSize);
        invalidate();
    }

    public int getYawBgColor() {
        return topBarBgPaint.getColor();
    }

    public void setYawBgColor(int color) {
        topBarBgPaint.setColor(color);
        invalidate();
    }

}
