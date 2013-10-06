package com.droidplanner.widgets.newHUD;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class newHUD extends View {

	private static final float YAW_ARROW_SIZE = 1.2f;
	private static final float  YAW_ARROW_ANGLE = 4.5f;
	private static final float INTERNAL_RADIUS = 0.95f;
	private float halfWidth;
	private float halfHeight;
	private float radiusExternal;
	private float radiusInternal;
	
	private Paint yawPaint;
	private Paint skyPaint;
	private Paint groundPaint;

	private float yaw = 0;
	private float roll = 30;
	private RectF groundPoints;
	
	public newHUD(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	private void initialize() {
		
		Paint fillPaint = new Paint();
		fillPaint.setAntiAlias(true);
		fillPaint.setStyle(Style.FILL);
		
		yawPaint = new Paint(fillPaint);
		yawPaint.setColor(Color.WHITE);
		
		skyPaint = new Paint(fillPaint);
		skyPaint.setColor(Color.parseColor("#008888"));
		
		groundPaint = new Paint(fillPaint);
		groundPaint.setColor(Color.parseColor("#723700"));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		halfHeight = h / 2f;
		halfWidth = w / 2f;
		radiusExternal = Math.min(halfHeight,halfWidth)/YAW_ARROW_SIZE;
		radiusInternal = radiusExternal*INTERNAL_RADIUS;
		groundPoints = new RectF(-radiusInternal, -radiusInternal, radiusInternal, radiusInternal);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.translate(halfWidth, halfHeight);
		drawYaw(canvas);

		canvas.drawCircle(0, 0, radiusInternal, skyPaint);
		canvas.drawArc(groundPoints, roll, 180, true, groundPaint);
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
