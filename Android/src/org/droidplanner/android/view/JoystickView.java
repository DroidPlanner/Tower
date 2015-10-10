package org.droidplanner.android.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import org.droidplanner.android.R;

/**
 * Created by Toby on 8/5/2015.
 */
public class JoystickView extends View {

    public enum Axis {
        X, Y
    }

    public interface JoystickListener {
        void joystickMoved(float x, float y);
    }

    private float x = 0f, y = 0f;
    private float uiX, uiY;
    private boolean springX, springY;
    private boolean lockedX, lockedY;
    private final Bitmap reticle;
    private JoystickListener listener;
    private boolean engaged;
    private ValueAnimator animator;
    private float major, minor;
    private static float MAX_SIZE = 300;
    private static float MIN_SIZE = 50;
    private static final int THRESHOLD = 200;
    public static final float DEADZONE = 0.05f;
    private static final float SPEED_THRESHOLD = 0.0005f;
    private long lastEvent;
    private final Vibrator vibrator;
    private boolean hapticX, hapticY;

    private final Paint reticlePaint = new Paint();
    private final Paint disabledReticlePaint = new Paint();
    {
        reticlePaint.setStrokeWidth(5f);
        reticlePaint.setARGB(255, 255, 128, 0);

        disabledReticlePaint.setStrokeWidth(5f);
        disabledReticlePaint.setColor(Color.GRAY);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if(isInEditMode()){
            vibrator = null;
        }
        else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

        MAX_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        MIN_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());

        major = MIN_SIZE;
        minor = MIN_SIZE;

        this.reticle = BitmapFactory.decodeResource(getResources(), R.drawable.ic_control_grey_600_24dp);
    }

    public JoystickView(Context context) {
        this(context, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isEnabled()){
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float top = (y + 1f) / 2f * getHeight();
                float left = (x + 1f) / 2f * getWidth();
                float dist = (float) Math.sqrt(Math.pow((double) (top - event.getY()), 2.0) + Math.pow((double) (left - event.getX()), 2.0));
                if (animator != null) {
                    animator.cancel();
                }
                if (dist < THRESHOLD) {
                    engaged = true;
                    if (vibrator.hasVibrator()) {
                        vibrator.vibrate(50);
                    }
                }
                lastEvent = System.currentTimeMillis();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (engaged) {
                    float delta = (System.currentTimeMillis() - lastEvent) / 1000f;
                    lastEvent = System.currentTimeMillis();
                    float lastY = y;
                    float lastX = x;
                    y = (((event.getY() * 2f) / getHeight()) - 1f);
                    x = ((event.getX() * 2f) / getWidth()) - 1f;
                    y = Math.min(1, y);
                    y = Math.max(-1, y);
                    x = Math.min(1, x);
                    x = Math.max(-1, x);
                    dispatchMove();
//                    float velocity = (float)Math.sqrt(Math.pow(Math.abs(lastY - y)*delta, 2) + Math.pow(Math.abs(lastX - x)*delta, 2));
//                    if(((((Math.abs(lastY) < DEADZONE && Math.abs(y) < DEADZONE)) && velocity< SPEED_THRESHOLD)
//                            || (((Math.abs(lastX) < DEADZONE && Math.abs(x) < DEADZONE)) && velocity < SPEED_THRESHOLD)) && !(lockedX || lockedY)){
//                        Timber.d("speed velocity: %f", velocity);
//                        if(Math.abs(x) < DEADZONE){
//                            x = 0f;
//                            lockedX = true;
//                            if(hapticX) {
//                                vibrator.vibrate(50);
//                            }
//                        }
//                        if(Math.abs(y) < DEADZONE){
//                            y = 0f;
//                            lockedY = true;
//                            if(hapticY) {
//                                vibrator.vibrate(50);
//                            }
//                        }
//                    }
//                    if(Math.abs(x) < DEADZONE && lockedX){
//                        x = 0f;
//                    }else{
//                        lockedX = false;
//                    }
//                    if(Math.abs(y) < DEADZONE && lockedY){
//                        y = 0f;
//                    }else{
//                        lockedY = false;
//                    }
                    invalidate();
                }
                major = Math.min(event.getTouchMajor(), MAX_SIZE);
                major = Math.max(major, MIN_SIZE);
                minor = Math.min(event.getTouchMinor(), MAX_SIZE);
                minor = Math.max(minor, MIN_SIZE);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                engaged = false;
                final float xStart = x, yStart = y;
                final float majorStart = major, minorStart = minor;
                if (springX || springY) {
                    animator = ValueAnimator.ofFloat(0f, 1f);
                    animator.setInterpolator(new AccelerateInterpolator());
                    animator.setDuration(500);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if (springX) {
                                x = (xStart * (1f - animation.getAnimatedFraction()));
                            }
                            if (springY) {
                                y = (yStart * (1f - animation.getAnimatedFraction()));
                            }
                            major = (majorStart - MIN_SIZE) * (1f - animation.getAnimatedFraction()) + MIN_SIZE;
                            minor = (minorStart - MIN_SIZE) * (1f - animation.getAnimatedFraction()) + MIN_SIZE;
                            dispatchMove();
                            invalidate();
                        }
                    });
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (springX) {
                                x = 0f;
                            }
                            if (springY) {
                                y = 0f;
                            }
                            dispatchMove();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            onAnimationEnd(animation);
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animator.start();
                }
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int canvasHeight = canvas.getHeight();
        final int canvasWidth = canvas.getWidth();

        float top = isEnabled() ? (y + 1f) / 2f * canvasHeight: canvasHeight / 2f;
        float left = isEnabled() ? (x + 1f) / 2f * canvasWidth : canvasWidth / 2f;
        final Paint paint = isEnabled() ? reticlePaint : disabledReticlePaint;

        canvas.drawBitmap(reticle, left - reticle.getWidth() / 2, top - reticle.getHeight() / 2, null);
        canvas.drawCircle(left, top, minor, paint);
        canvas.drawLine(canvasWidth / 2f, canvasHeight / 2f, left, top, paint);
    }

    public void setSpring(Axis axis, boolean spring) {
        switch (axis) {
            case X:
                springX = spring;
                break;

            case Y:
                springY = spring;
                break;
        }
    }

    public boolean getSpring(Axis axis) {
        switch (axis) {
            case X:
                return springX;
            case Y:
                return springY;
        }
        return false;
    }

    public float getAxis(Axis axis) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
        }
        return 1f;
    }

    public void setJoystickListener(JoystickListener listener) {
        this.listener = listener;
    }

    private void dispatchMove() {
        if (listener != null && isEnabled()) {
            listener.joystickMoved(x, -y);

        }
    }

    public void setHaptic(Axis axis, boolean haptic) {
        switch (axis) {
            case X:
                hapticX = haptic;
                break;
            case Y:
                hapticY = haptic;
                break;
        }
    }

    private float mapAxis(float value) {
        float absVal = Math.abs(value);
        if (absVal < DEADZONE) {
            return 0;
        }

        absVal -= .05f;
        absVal /= .095f;
        return (float) Math.pow(absVal, 2) * Math.signum(value);
    }
}
