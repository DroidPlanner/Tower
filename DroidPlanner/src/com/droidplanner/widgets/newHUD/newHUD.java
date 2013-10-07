package com.droidplanner.widgets.newHUD;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class newHUD extends View {

	private static final float INTERNAL_RADIUS = 0.95f;
	private static final float YAW_ARROW_SIZE = 1.2f;
	private static final float YAW_ARROW_ANGLE = 4.5f;
	private static final float PITCH_TICK_LINE_LENGTH = 0.4f;
	private static final int PITCH_RANGE = 45;
	private static final int PITCH_TICK_SPACING = 15;
	private static final int PITCH_TICK_PADDING = 2;
	private static final float PLANE_BODY_SIZE = 0.2f;
	private static final float PLANE_WING_WIDTH = 0.05f;
	private static final float PLANE_SIZE = 0.8f;

	private float halfWidth;
	private float halfHeight;
	private float radiusExternal;
	private float radiusInternal;
	private RectF internalBounds;

	private Paint yawPaint;
	private Paint skyPaint;
	private Paint groundPaint;
	private Paint planePaint;

	private Path yawPath = new Path();
	private Path groundPath = new Path();
	private Path planePath = new Path();

	private float yaw = -45;
	private float roll = -30;
	private float pitch = 20;
	private Paint tickPaint;

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

		groundPaint = new Paint(fillPaint);
		groundPaint.setColor(Color.parseColor("#723700"));

		planePaint = new Paint(fillPaint);
		planePaint.setColor(Color.WHITE);

		tickPaint = new Paint(fillPaint);
		tickPaint.setColor(Color.WHITE);
		tickPaint.setStrokeWidth(2);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		halfHeight = h / 2f;
		halfWidth = w / 2f;
		radiusExternal = Math.min(halfHeight, halfWidth) / YAW_ARROW_SIZE;
		radiusInternal = radiusExternal * INTERNAL_RADIUS;
		internalBounds = new RectF(-radiusInternal, -radiusInternal,
				radiusInternal, radiusInternal);
		buildPlanePath();
		skyPaint.setShader(new LinearGradient(0, -radiusInternal, 0,
				radiusInternal, Color.parseColor("#004444"), Color
						.parseColor("#00FFFF"), TileMode.CLAMP));

	}

	private void buildPlanePath() {
		planePath.reset();
		planePath.moveTo(0, radiusInternal * PLANE_SIZE * PLANE_WING_WIDTH);
		planePath.lineTo(radiusInternal * PLANE_SIZE, 0);
		planePath.lineTo(0, -radiusInternal * PLANE_SIZE * PLANE_WING_WIDTH);
		planePath.lineTo(-radiusInternal * PLANE_SIZE, 0);
		planePath.lineTo(0, radiusInternal * PLANE_SIZE * PLANE_WING_WIDTH);
		planePath.moveTo(radiusInternal * PLANE_SIZE * PLANE_WING_WIDTH, 0);
		planePath.lineTo(0, -radiusInternal * PLANE_SIZE / 2);
		planePath.lineTo(-radiusInternal * PLANE_SIZE * PLANE_WING_WIDTH, 0);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.translate(halfWidth, halfHeight);
		drawYaw(canvas);
		drawSkyAndGround(canvas);
		drawPitchTicks(canvas);
		drawPlane(canvas);
	}

	private void drawYaw(Canvas canvas) {
		// Fill the background
		canvas.drawCircle(0, 0, radiusExternal, yawPaint);

		// Yaw Arrow
		float mathYaw = (float) Math.toRadians(180 - yaw);
		yawPath.reset();
		yawPath.moveTo(0, 0);
		radialLineTo(yawPath, mathYaw + YAW_ARROW_ANGLE, radiusExternal);
		radialLineTo(yawPath, mathYaw, radiusExternal * YAW_ARROW_SIZE);
		radialLineTo(yawPath, mathYaw - YAW_ARROW_ANGLE, radiusExternal);
		canvas.drawPath(yawPath, yawPaint);
	}

	private void radialLineTo(Path path, float angle, float radius) {
		path.lineTo((float) Math.sin(angle) * radius, (float) Math.cos(angle)
				* radius);
	}

	private void drawSkyAndGround(Canvas canvas) {
		// Fill with the sky
		canvas.drawCircle(0, 0, radiusInternal, skyPaint);

		// Overlay the ground
		groundPath.reset();
		float pitchProjection = (float) Math.toDegrees(Math.acos(pitch
				/ PITCH_RANGE));
		groundPath.addArc(internalBounds, 90 - pitchProjection + roll,
				pitchProjection * 2);
		canvas.drawPath(groundPath, groundPaint);

	}

	private void drawPitchTicks(Canvas canvas) {
		float lineX = (float) (Math.cos(Math.toRadians(roll)) * radiusInternal)
				* PITCH_TICK_LINE_LENGTH;
		float lineY = (float) (Math.sin(Math.toRadians(roll)) * radiusInternal)
				* PITCH_TICK_LINE_LENGTH;
		float dx = (float) (Math.cos(Math.toRadians(roll - 90))
				* radiusInternal / PITCH_RANGE);
		float dy = (float) (Math.sin(Math.toRadians(roll - 90))
				* radiusInternal / PITCH_RANGE);
		int i = (int) ((-PITCH_RANGE + pitch + PITCH_TICK_PADDING) / PITCH_TICK_SPACING);
		int loopEnd = (int) ((PITCH_RANGE + pitch - PITCH_TICK_PADDING) / PITCH_TICK_SPACING);
		for (; i <= loopEnd; i++) {
			float degree = -pitch + PITCH_TICK_SPACING * i;
			canvas.drawLine(lineX + dx * degree, lineY + dy * degree, -lineX
					+ dx * degree, -lineY + dy * degree, tickPaint);
		}
	}

	private void drawPlane(Canvas canvas) {
		canvas.drawPath(planePath, planePaint);
		canvas.drawCircle(0, 0, radiusInternal * PLANE_SIZE * PLANE_BODY_SIZE,
				planePaint);
	}

	public void setAttitude(float roll, float pitch, float yaw) {
		this.roll = roll;
		this.pitch = pitch;
		this.yaw = yaw;
	}
}
