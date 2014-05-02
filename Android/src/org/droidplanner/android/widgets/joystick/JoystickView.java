/**
 * Copied from https://code.google.com/p/mobile-anarchy-widgets/
 */
package org.droidplanner.android.widgets.joystick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
	public static final int INVALID_POINTER_ID = -1;
	public String TAG = "JoystickView";

	private static final double HAPTIC_FEEDBACK_ZONE = 0.05;
	private int handleRadius = 20;
	private int movementRadius = handleRadius * 4;

	private boolean yAxisInverted = false;
	private boolean xAxisInverted = false;
	private boolean yAxisAutoReturnToCenter = true;
	private boolean xAxisAutoReturnToCenter = true;
	private boolean autoReturnToCenter = true;

	private Paint bgHandlePaint;
	private Paint handlePaint;

	private JoystickMovedListener moveListener;

	// Last touch point in view coordinates
	private int pointerId = INVALID_POINTER_ID;
	private float touchX, touchY;

	// Cartesian coordinates of last touch point - joystick center is (0,0)
	private double cartX, cartY;

	// User coordinates of last touch point
	private double userX, userY;
	private double userXold, userYold;

	private float firstTouchX, firstTouchY;
	private double releaseX = 0;
	private double releaseY = 0;

	private boolean handleVisible = false;

	public JoystickView(Context context) {
		super(context);
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initJoystickView();
	}

	private void initJoystickView() {
		setFocusable(true);
		setHapticFeedbackEnabled(true);

		handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		handlePaint.setColor(Color.BLACK);
		handlePaint.setStrokeWidth(1);
		handlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		bgHandlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bgHandlePaint.setColor(Color.BLUE);
		bgHandlePaint.setStrokeWidth(1);
		bgHandlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
	}

	public void setAutoReturnToCenter(boolean autoReturnToCenter) {
		this.autoReturnToCenter = autoReturnToCenter;
	}

	public boolean isAutoReturnToCenter() {
		return autoReturnToCenter;
	}

	public boolean isXAxisInverted() {
		return xAxisInverted;
	}

	public boolean isYAxisInverted() {
		return yAxisInverted;
	}

	public void setXAxisInverted(boolean xAxisInverted) {
		this.xAxisInverted = xAxisInverted;
	}

	public void setYAxisInverted(boolean yAxisInverted) {
		this.yAxisInverted = yAxisInverted;
	}

	public void setOnJostickMovedListener(JoystickMovedListener listener) {
		this.moveListener = listener;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();

		// Draw the handle
		if (handleVisible) {
			canvas.drawCircle(firstTouchX, firstTouchY, movementRadius,
					bgHandlePaint);
			canvas.drawCircle(firstTouchX, firstTouchY, handleRadius,
					handlePaint);
		}
		canvas.restore();
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int pointerIndex;
		int pointerId;
		final int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_MOVE:
			return processMove(ev);
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (isPointerValid()) {
				return processRelease();
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			pointerId = ev.getPointerId(pointerIndex);
			if (pointerId == this.pointerId) {
				return processRelease();
			}
			break;
		case MotionEvent.ACTION_DOWN:
			if (!isPointerValid()) {
				this.pointerId = ev.getPointerId(0);
				processFirstTouch(ev);
				return true;
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			pointerId = ev.getPointerId(pointerIndex);
			if (pointerId == INVALID_POINTER_ID) {
				this.pointerId = pointerId;
				processFirstTouch(ev);
				return true;
			}
			break;
		}
		return false;
	}

	private boolean processRelease() {
		this.pointerId = INVALID_POINTER_ID;
		handleVisible = false;
		invalidate();
		if (moveListener != null) {
			releaseX = xAxisAutoReturnToCenter ? 0 : userX;
			releaseY = yAxisAutoReturnToCenter ? 0 : userY;
			moveListener.OnMoved(releaseX, releaseY);
		}
		return true;
	}

	private void processFirstTouch(MotionEvent ev) {
		firstTouchX = ev.getX();
		firstTouchY = ev.getY();
		touchX = 0;
		touchY = 0;
		handleVisible = true;
		invalidate();
	}

	private boolean isPointerValid() {
		return pointerId != INVALID_POINTER_ID;
	}

	private boolean processMove(MotionEvent ev) {
		if (isPointerValid()) {
			final int pointerIndex = ev.findPointerIndex(pointerId);

			// Translate touch position to center of view
			float x = ev.getX(pointerIndex);
			float y = ev.getY(pointerIndex);

			touchX = x - firstTouchX;
			touchY = y - firstTouchY;

			reportOnMoved();
			return true;
		}
		return false;
	}

	private void reportOnMoved() {
		calcUserCoordinates();
		constrainBox();

		hapticFeedback();

		if (moveListener != null) {
			moveListener.OnMoved(userX, userY);
		}
	}

	private void hapticFeedback() {
		if (hasEnteredHapticFeedbackZone(userX, userXold)) {
			performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			Log.d(TAG, "XonCenter");
		}
		if (hasEnteredHapticFeedbackZone(userY, userYold)) {
			performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			Log.d(TAG, "YonCenter");
		}

		userXold = userX;
		userYold = userY;
	}

	private boolean hasEnteredHapticFeedbackZone(double value, double oldValue) {
		return isInHapticFeedbackZone(value)
				& (!isInHapticFeedbackZone(oldValue));
	}

	private boolean isInHapticFeedbackZone(double value) {
		return Math.abs(value) < HAPTIC_FEEDBACK_ZONE;
	}

	private void calcUserCoordinates() {
		// First convert to cartesian coordinates
		cartX = (touchX / movementRadius);
		cartY = (touchY / movementRadius);

		// Invert axis if requested
		if (!xAxisInverted)
			cartX *= -1;
		if (!yAxisInverted)
			cartY *= -1;

		userX = cartX + (xAxisAutoReturnToCenter ? 0 : releaseX);
		userY = cartY + (yAxisAutoReturnToCenter ? 0 : releaseY);

	}

	// Constrain touch within a box
	private void constrainBox() {
		userX = Math.max(Math.min(userX, 1), -1);
		userY = Math.max(Math.min(userY, 1), -1);
	}

	public void setAxisAutoReturnToCenter(boolean yAxisAutoReturnToCenter,
			boolean xAxisAutoReturnToCenter) {
		this.yAxisAutoReturnToCenter = yAxisAutoReturnToCenter;
		this.xAxisAutoReturnToCenter = xAxisAutoReturnToCenter;
	}
}
