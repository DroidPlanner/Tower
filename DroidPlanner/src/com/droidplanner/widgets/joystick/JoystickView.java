/**
 * Copied from https://code.google.com/p/mobile-anarchy-widgets/
 */
package com.droidplanner.widgets.joystick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
	public static final int INVALID_POINTER_ID = -1;
	public String TAG = "JoystickView";
	
	private int handleRadius = 20;
	private Paint bgHandlePaint;
	private int movementRadius = handleRadius * 4;
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
	private double releaseX = 0;
	private double releaseY = 0;

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
		handlePaint.setColor(Color.BLACK);
		handlePaint.setStrokeWidth(1);
		handlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		
		bgHandlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bgHandlePaint.setColor(Color.BLUE);
		bgHandlePaint.setStrokeWidth(1);
		bgHandlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

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
			canvas.drawCircle(firstTouchX, firstTouchY, movementRadius, bgHandlePaint);		
			canvas.drawCircle(firstTouchX, firstTouchY, handleRadius, handlePaint);		
		}
		canvas.restore();
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
			releaseX = xAxisAutoReturnToCenter?0:userX;
			releaseY = yAxisAutoReturnToCenter?0:userY;
			moveListener.OnMoved(releaseX, releaseY);
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
			
			reportOnMoved();
			return true;
		}
		return false;
	}

	private void reportOnMoved() {
		calcUserCoordinates();
		constrainBox();

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

		userX = cartX + (xAxisAutoReturnToCenter?0:releaseX);
		userY = cartY + (yAxisAutoReturnToCenter?0:releaseY);
		
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
