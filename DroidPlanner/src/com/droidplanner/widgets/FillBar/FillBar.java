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
	private int colorOutline;
	private int colorMax;
	private int colorMin;
	private int colorBar;

	public FillBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	private void initialize() {
		colorOutline = Color.parseColor("#E0E0E0");
		colorMin = Color.parseColor("#FF0E0E");
		colorMax = Color.parseColor("#FF0E0E");
		colorBar = Color.parseColor("#E0E0E0");

		paintOutline = new Paint();
		paintOutline.setAntiAlias(false);
		paintOutline.setStyle(Style.STROKE);
		paintOutline.setStrokeWidth(3);

		paintFill = new Paint(paintOutline);
		paintFill.setStyle(Style.FILL_AND_STROKE);
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

		paintOutline.setColor(colorOutline);
		outlinePath.reset();
		outlinePath.moveTo(0, 0);
		outlinePath.lineTo(0, height);
		outlinePath.lineTo(width, height);
		outlinePath.lineTo(width, 0);
		outlinePath.lineTo(0, 0);
		canvas.drawPath(outlinePath, paintOutline);

		paintFill.setColor(colorBar);
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
			outlinePath.reset();
			if (height > width) {
				f = height * (1-min);

				outlinePath.reset();
				outlinePath.moveTo(0, f);
				outlinePath.lineTo(width, f);
				paintOutline.setColor(colorMin);
				canvas.drawPath(outlinePath, paintOutline);
				
				outlinePath.reset();
				f = height * (1-max);
				outlinePath.moveTo(0, f);
				outlinePath.lineTo(width, f);
				paintOutline.setColor(colorMax);
				canvas.drawPath(outlinePath, paintOutline);
			} else {
				f = width * min;
				outlinePath.reset();
				outlinePath.moveTo(f, 0);
				outlinePath.lineTo(f, height);
				paintOutline.setColor(colorMin);
				canvas.drawPath(outlinePath, paintOutline);

				f = width * max;
				outlinePath.reset();
				outlinePath.moveTo(f, 0);
				outlinePath.lineTo(f, height);
				paintOutline.setColor(colorMax);
				canvas.drawPath(outlinePath, paintOutline);
			}
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

	public int getColorOutline() {
		return colorOutline;
	}

	public void setColorOutline(int colorOutline) {
		this.colorOutline = colorOutline;
	}

	public int getColorMax() {
		return colorMax;
	}

	public void setColorMax(int colorMax) {
		this.colorMax = colorMax;
	}

	public int getColorMin() {
		return colorMin;
	}

	public void setColorMin(int colorMin) {
		this.colorMin = colorMin;
	}

	public int getColorBar() {
		return colorBar;
	}

	public void setColorBar(int colorBar) {
		this.colorBar = colorBar;
	}
}
