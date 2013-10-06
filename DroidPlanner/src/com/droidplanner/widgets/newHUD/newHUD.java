package com.droidplanner.widgets.newHUD;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class newHUD extends View {

	private static final float YAW_ARROW_SIZE = 1.2f;
	private static final float  YAW_ARROW_ANGLE = 4.5f;
	private static final float INTERNAL_RADIUS = 0.95f;
	private Paint yawPaint;
	private float halfWidth;
	private float halfHeight;
	private int yaw = 0;
	private float radiusExternal;
	private float radiusInternal;
	private Paint skyPaint;

	public newHUD(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	private void initialize() {
		yawPaint = new Paint();
		yawPaint.setAntiAlias(true);
		yawPaint.setColor(Color.WHITE);
		yawPaint.setStyle(Style.FILL);
		
		skyPaint = new Paint();
		skyPaint.setAntiAlias(true);
		skyPaint.setColor(Color.BLUE);
		skyPaint.setStyle(Style.FILL);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		halfHeight = h / 2f;
		halfWidth = w / 2f;
		radiusExternal = Math.min(halfHeight,halfWidth)/YAW_ARROW_SIZE;
		radiusInternal = radiusExternal*INTERNAL_RADIUS;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.translate(halfWidth, halfHeight);
		drawYaw(canvas);

		canvas.drawCircle(0, 0, radiusInternal, skyPaint);
	}

	private void drawYaw(Canvas canvas) {
		canvas.drawCircle(0, 0, radiusExternal, yawPaint);
		
		// Yaw Arrow
		Path path = new Path();
		path.moveTo(0, 0);
		path.lineTo((float) Math.sin(yaw - YAW_ARROW_ANGLE) * radiusExternal,
				(float) Math.cos(yaw - YAW_ARROW_ANGLE) * radiusExternal);
		path.lineTo((float) Math.sin(yaw) * radiusExternal * 1.1f,
				(float) Math.cos(yaw) * radiusExternal * YAW_ARROW_SIZE);
		path.lineTo((float) Math.sin(yaw + YAW_ARROW_ANGLE) * radiusExternal,
				(float) Math.cos(yaw + YAW_ARROW_ANGLE) * radiusExternal);
		canvas.drawPath(path, yawPaint);

	}
}
