package org.droidplanner.android.widgets;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import timber.log.Timber;

/**
 * Created by Toby on 8/5/2015.
 */
public class JoystickView extends View {
    private float x = 0f,y = 0f;
    private boolean springX, springY;
    private Bitmap reticle;
    private JoystickListener listener;
    private boolean engaged;
    private ValueAnimator animator;
    private float major, minor;
    private static final float MAX_SIZE = 300;
    private static final int THRESHOLD = 200;
    private Vibrator vibrator;

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public enum Axis {
        X, Y
    }

    public interface JoystickListener{
        void joystickMoved(float x, float y);
    }
    public JoystickView(Context context) {
        super(context);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setReticle(Bitmap reticle){
        this.reticle = reticle;
    }

    public Bitmap getReticle(){
        return reticle;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                float top = (y + 1f)/2f * getHeight();
                float left = (x+ 1f)/2f * getWidth();
                float dist = (float)Math.sqrt(Math.pow((double)(top - event.getY()), 2.0) + Math.pow((double)(left- event.getX()), 2.0));
                if(animator != null){
                    animator.cancel();
                }
                if(dist < THRESHOLD){
                    engaged = true;
                    if(vibrator.hasVibrator()) {
                        vibrator.vibrate(50);
                    }
                }

                return true;
            case MotionEvent.ACTION_MOVE:
                if(engaged) {
                    y = (((event.getY() * 2f) / getHeight()) - 1f);
                    x = ((event.getX() * 2f) / getWidth()) - 1f;
                    y = Math.min(1, y);
                    y = Math.max(-1, y);
                    x = Math.min(1, x);
                    x = Math.max(-1, x);
                    invalidate();
                    dispatchMove();
                }
                major = Math.min(event.getTouchMajor(), MAX_SIZE);
                minor = Math.min(event.getTouchMinor(), MAX_SIZE);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                engaged = false;
                final float xStart = x, yStart = y;
                final float majorStart= major, minorStart = minor;
                if(springX  || springY) {
                    animator = ValueAnimator.ofFloat(0f, 1f);
                    animator.setInterpolator(new AccelerateInterpolator());
                    animator.setDuration(100);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if (springX) {
                                x = (xStart * (1f - animation.getAnimatedFraction()));
                            }
                            if (springY) {
                                y = (yStart * (1f - animation.getAnimatedFraction()));
                            }
                            major = majorStart * (1f - animation.getAnimatedFraction());
                            minor = minorStart * (1f - animation.getAnimatedFraction());
                            invalidate();
                        }
                    });
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if(springX) {
                                x = 0f;
                            }
                            if(springY) {
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
        float top = (y + 1f)/2f * canvas.getHeight();
        float left = (x+ 1f)/2f * canvas.getWidth();
        canvas.drawBitmap(reticle, left - reticle.getWidth() / 2, top - reticle.getHeight() / 2, null);
        Paint paint = new Paint();
        paint.setStrokeWidth(5f);
        paint.setARGB(255, 255, 128, 0);
        canvas.drawCircle(left, top, minor, paint);
        canvas.drawLine(canvas.getWidth() / 2f, canvas.getHeight() / 2f, left, top, paint);
    }

    public void setSpring(Axis axis, boolean spring){
        switch (axis){
            case X:
                springX = spring;
                break;
            case Y:
                springY = spring;
                break;
        }
    }

    public boolean getSpring(Axis axis){
        switch (axis){
            case X:
                return springX;
            case Y:
                return springY;
        }
        return false;
    }

    public float getAxis(Axis axis){
        switch (axis){
            case X:
                return x;
            case Y:
                return y;
        }
        return 1f;
    }

    public void setJoystickListener(JoystickListener listener){
        this.listener = listener;
    }

    private void dispatchMove(){
        if(listener != null){
            listener.joystickMoved(x, -y);

        }
    }

    private float mapAxis(float value){
        float absVal = Math.abs(value);
        if(absVal < 0.05f){
            return 0;
        }
        absVal -= .05f;
        absVal /= .095f;
        return (float)Math.pow(absVal, 2) * Math.signum(value);
    }


}
