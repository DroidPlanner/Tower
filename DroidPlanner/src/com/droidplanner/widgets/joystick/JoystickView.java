/**
 * Copied from https://code.google.com/p/mobile-anarchy-widgets/
 */
package com.droidplanner.widgets.joystick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
	public static final int INVALID_POINTER_ID = -1;
	public String TAG = "JoystickView";
	
	private int handleRadius = 20;
	private int movementRadius = handleRadius * 3;
	// # of pixels movement required between reporting to the listener
	private float moveResolution = 1;
	
	private boolean yAxisInverted;
	private boolean xAxisInverted;
	private boolean yAxisAutoReturnToCenter = true;
	private boolean xAxisAutoReturnToCenter = true;
	private boolean autoReturnToCenter;

	private Paint handlePaint;

	private JoystickMovedListener moveListener;




	// Last touch point in view coordinates
	private int pointerId = INVALID_POINTER_ID;
	private float touchX, touchY;

	// Last reported position in view coordinates (allows different reporting
	// sensitivities)
	private float reportX, reportY;

	// Cartesian coordinates of last touch point - joystick center is (0,0)
	private double cartX, cartY;

	// User coordinates of last touch point
	private double userX, userY;

	private float firstTouchX;

	private float firstTouchY;

	private boolean handleVisible = false;

	// =========================================
	// Constructors
	// =========================================

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

	// =========================================
	// Initialization
	// =========================================

	private void initJoystickView() {
		setFocusable(true);

		handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		handlePaint.setColor(Color.DKGRAY);
		handlePaint.setStrokeWidth(1);
		handlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		setXAxisInverted(false);
		setYAxisInverted(false);
		setAutoReturnToCenter(true);
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

	// =========================================
	// Public Methods
	// =========================================

	public void setOnJostickMovedListener(JoystickMovedListener listener) {
		this.moveListener = listener;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();

		// Draw the handle
		if (handleVisible) {
			canvas.drawCircle(firstTouchX, firstTouchY, handleRadius, handlePaint);			
		}
		canvas.restore();
	}

	// Constrain touch within a box
	private void constrainBox() {
		touchX = Math.max(Math.min(touchX, movementRadius), -movementRadius);
		touchY = Math.max(Math.min(touchY, movementRadius), -movementRadius);
	}

	public void setPointerId(int id) {
		this.pointerId = id;
	}

	public int getPointerId() {
		return pointerId;
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
				setPointerId(ev.getPointerId(0));
				processFirstTouch(ev);
				return true;
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			pointerId = ev.getPointerId(pointerIndex);
			if (pointerId == INVALID_POINTER_ID) {
				setPointerId(pointerId);
				processFirstTouch(ev);
				return true;
			}
			break;
		}
		return false;
	}

	private boolean processRelease() {
		setPointerId(INVALID_POINTER_ID);
		handleVisible = false;
		invalidate();
		if (moveListener!=null) {
			moveListener.OnMoved(0, 0);
		}
		return true;
	}

	private void processFirstTouch(MotionEvent ev) {
		firstTouchX = ev.getX();
		firstTouchY = ev.getY();
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
			Log.d(TAG, String.format(
					"ACTION_MOVE: (%03.0f, %03.0f) => (%03.0f, %03.0f)", x, y,
					touchX, touchY));

			reportOnMoved();
			return true;
		}
		return false;
	}

	private void reportOnMoved() {
		constrainBox();

		calcUserCoordinates();

		if (moveListener != null) {
			boolean rx = Math.abs(touchX - reportX) >= moveResolution;
			boolean ry = Math.abs(touchY - reportY) >= moveResolution;
			if (rx || ry) {
				this.reportX = touchX;
				this.reportY = touchY;

				moveListener.OnMoved(userX, userY);
			}
		}
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
		userX = cartX;
		userY = cartY;
	}

	private void returnHandleToCenter() {
		if (autoReturnToCenter) {
			final int numberOfFrames = 5;
			final double intervalsX = (0 - touchX) / numberOfFrames;
			final double intervalsY = (0 - touchY) / numberOfFrames;

			for (int i = 0; i < numberOfFrames; i++) {
				final int j = i;
				postDelayed(new Runnable() {
					@Override
					public void run() {
						if (xAxisAutoReturnToCenter) {
							touchX += intervalsX;
						}
						if (yAxisAutoReturnToCenter) {
							touchY += intervalsY;
						}

						reportOnMoved();
						invalidate();

						if (moveListener != null && j == numberOfFrames - 1) {
							moveListener.OnReturnedToCenter();
						}
					}
				}, i * 40);
			}

			if (moveListener != null) {
				moveListener.OnReleased();
			}
		}
	}

	public void setAxisAutoReturnToCenter(boolean yAxisAutoReturnToCenter,
			boolean xAxisAutoReturnToCenter) {
		this.yAxisAutoReturnToCenter = yAxisAutoReturnToCenter;
		this.xAxisAutoReturnToCenter = xAxisAutoReturnToCenter;
	}
}
