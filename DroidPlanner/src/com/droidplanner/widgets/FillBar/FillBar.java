package com.droidplanner.widgets.FillBar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class FillBar extends View {

	private Paint paintOutline;
	private Paint paintFill;
	private Path outlinePath = new Path();
	private Path fillPath = new Path();
	private int height;
	private int width;
	private float percentage = 0.5f;

	
	public FillBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	private void initialize() {

		paintOutline = new Paint();
		paintOutline.setAntiAlias(false);
		paintOutline.setStyle(Style.STROKE);
		paintOutline.setStrokeWidth(3);
		paintOutline.setColor(Color.parseColor("#E0E0E0"));

		paintFill = new Paint(paintOutline);
		paintFill.setStyle(Style.FILL);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w - 1;
		height = h - 1;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Yaw Arrow
		outlinePath.reset();
		outlinePath.moveTo(0, 0);
		outlinePath.lineTo(0, height);
		outlinePath.lineTo(width, height);
		outlinePath.lineTo(width, 0);
		outlinePath.lineTo(0, 0);
		canvas.drawPath(outlinePath, paintOutline);

		float fillHeight = height * (1 - percentage);
		fillPath.reset();
		fillPath.moveTo(0, fillHeight);
		fillPath.lineTo(0, height);
		fillPath.lineTo(width, height);
		fillPath.lineTo(width, fillHeight);
		fillPath.lineTo(0, fillHeight);
		canvas.drawPath(fillPath, paintFill);
	}

	public float getPercentage() {
		return percentage;
	}

	public void setPercentage(float percentage) {
		this.percentage = percentage;
		invalidate();
	}
}
