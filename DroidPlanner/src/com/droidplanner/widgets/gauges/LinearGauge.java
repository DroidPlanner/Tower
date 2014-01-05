package com.droidplanner.widgets.gauges;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

public class LinearGauge extends View {

	private static final int TICK_WIDTH = 1;
	private static final float TRIANGLE_SIZE = 0.15f;

	private static final float RANGE = 7f;

	private int w;
	private int h;
	private float dy;
	private Paint ticksPaint;
	private Paint backgroundPaint;
	private Path triangle;
	private Path background;
	private Paint textPaint;

	private float value = 3.7f;
	private float target = 6f;
	
	public LinearGauge(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
		// setAttitude(0);
	}

	private void initialize(Context context) {
		ticksPaint = new Paint();
		ticksPaint.setAntiAlias(true);
		ticksPaint.setStrokeWidth(TICK_WIDTH);
		ticksPaint.setStrokeCap(Cap.ROUND);

		textPaint = new Paint(ticksPaint);
		textPaint.setTextAlign(Align.RIGHT);
		textPaint.setTextSize(20);

		triangle = new Path();
		background = new Path();

		backgroundPaint = new Paint();
		backgroundPaint.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawPath(background, backgroundPaint);

		redrawTargetMark(canvas);
		drawTicks(canvas);
	}

	private void drawTicks(Canvas canvas) {
		for (float tick = Math.round(value - RANGE / 2f); tick < value + RANGE / 2f; tick = tick + 0.5f) {
			float y = getVerticalPostition(tick);
			canvas.drawLine(w / 2f, y, w, y, ticksPaint);
			if (tick % 1 == 0) {
				canvas.drawText(String.format("%1.0f", tick), w*0.4f,
						y -3f + textPaint.getTextSize() / 2f , textPaint);
			}
		}
	}

	private float getVerticalPostition(float value) {
		return (value-this.value) * dy + h / 2f;
	}

	private void redrawTargetMark(Canvas canvas) {
		float dy = getVerticalPostition(target);
		triangle.rewind();
		triangle.moveTo(w, TRIANGLE_SIZE * w / 2f + dy);
		triangle.lineTo(w - TRIANGLE_SIZE * w, 0 + dy);
		triangle.lineTo(w, -TRIANGLE_SIZE * w / 2f + dy);
		triangle.close();
		canvas.drawPath(triangle, ticksPaint);
		canvas.drawLine(w / 2f, dy, w, dy, ticksPaint);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.w = w;
		this.h = h;
		this.dy = (-h / RANGE);

		backgroundPaint.setShader(new LinearGradient(0, 0, 0, h / 2, Color
				.argb(0, 255, 255, 255), Color.argb(100, 255, 255, 255),
				TileMode.MIRROR));

		background.rewind();
		background.moveTo(0, 0);
		background.lineTo(0, -TRIANGLE_SIZE * w / 2f + h / 2);
		background.lineTo(TRIANGLE_SIZE * w, h / 2);
		background.lineTo(0, TRIANGLE_SIZE * w / 2f + h / 2);
		background.lineTo(0, h);
		background.lineTo(w, h);
		background.lineTo(w, 0);
		background.close();
	}

	public void setValueAndTarget(float value, float target) {
		this.value = value;
		this.target = target;
		invalidate();
	}

}
