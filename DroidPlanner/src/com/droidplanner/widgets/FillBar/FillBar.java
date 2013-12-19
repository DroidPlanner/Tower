package com.droidplanner.widgets.FillBar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class FillBar extends View {

	private Paint paintOutline;
	private Paint paintFill;
	private Path outlinePath = new Path();
	private Path fillPath = new Path();
	private int height;
	private int width;
	private float percentage = 0.5f;
	private float fheight;
	private float fwidth;
	private float min = 0.5f;
	private float max = 0.5f;
	private boolean showMinMax = true;

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

		fheight = height < width ? height : (height * (1 - percentage));
		fwidth = height < width ? (width * (percentage)) : width;

		paintOutline.setColor(Color.parseColor("#E0E0E0"));
		outlinePath.reset();
		outlinePath.moveTo(0, 0);
		outlinePath.lineTo(0, height);
		outlinePath.lineTo(width, height);
		outlinePath.lineTo(width, 0);
		outlinePath.lineTo(0, 0);
		canvas.drawPath(outlinePath, paintOutline);

		fillPath.reset();
		if (height > width) {
			fillPath.moveTo(0, fheight);
			fillPath.lineTo(0, height);
			fillPath.lineTo(fwidth, height);
			fillPath.lineTo(fwidth, fheight);
			fillPath.lineTo(0, fheight);
		} else {
			fillPath.moveTo(0, 0);
			fillPath.lineTo(0, height);
			fillPath.lineTo(fwidth, height);
			fillPath.lineTo(fwidth, 0);
			fillPath.lineTo(0, 0);
		}
		canvas.drawPath(fillPath, paintFill);

		if (isShowMinMax()) {
			float f;
			paintOutline.setColor(Color.parseColor("#F40404"));
			outlinePath.reset();
			if (height > width) {
				f = height * (1-min);
				outlinePath.moveTo(0, f);
				outlinePath.lineTo(width, f);
				f = height * (1-max);
				outlinePath.moveTo(0, f);
				outlinePath.lineTo(width, f);
			} else {
				f = width * min;
				outlinePath.moveTo(f, 0);
				outlinePath.lineTo(f, height);
				f = height * max;
				outlinePath.moveTo(f, 0);
				outlinePath.lineTo(f, height);
			}
			canvas.drawPath(outlinePath, paintOutline);
		}
	}

	public float getPercentage() {
		return percentage;
	}

	public void setPercentage(float percentage) {
		this.percentage = percentage;
		this.min = this.min > percentage ? percentage : this.min;
		this.max = this.max < percentage ? percentage : this.max;
		Log.d("fillbar", "Min: " + String.valueOf(min));
		Log.d("fillbar", "Max: " + String.valueOf(max));

		invalidate();
	}

	public boolean isShowMinMax() {
		return showMinMax;
	}

	public void setShowMinMax(boolean showMinMax) {
		this.showMinMax = showMinMax;
		if (showMinMax) {
			min = 0.5f;
			max = 0.5f;
		}
	}
}
