package org.droidplanner.android.widgets.FillBar;

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
	private float fheight;
	private float fwidth;
	private float min = 0.5f;
	private float max = 0.5f;
	private boolean showMinMax = false;
	private int colorOutline;
	private int colorMin;
	private int colorBar;
	private int val_max, val_min;
	private boolean invert = false;

	public FillBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	private void initialize() {
		colorOutline = Color.parseColor("#333333");
		colorMin = Color.parseColor("#FFFFFF");
		colorBar = Color.parseColor("#3CB4E5");

		paintOutline = new Paint();
		paintOutline.setAntiAlias(false);
		paintOutline.setStyle(Style.STROKE);
		paintOutline.setStrokeWidth(3);

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

		paintFill.setColor(colorOutline);
		outlinePath.reset();
		outlinePath.moveTo(0, 0);
		outlinePath.lineTo(0, height);
		outlinePath.lineTo(width, height);
		outlinePath.lineTo(width, 0);
		outlinePath.lineTo(0, 0);
		canvas.drawPath(outlinePath, paintFill);

		paintFill.setColor(colorBar);
		fillPath.reset();

		if (invert) {
			if (height > width) {
				fillPath.moveTo(0, 0);
				fillPath.lineTo(0, height - fheight);
				fillPath.lineTo(width, height - fheight);
				fillPath.lineTo(width, 0);
				fillPath.lineTo(0, 0);
			} else {
				fillPath.moveTo(0, 0);
				fillPath.lineTo(0, height);
				fillPath.lineTo(width - fwidth, height);
				fillPath.lineTo(width - fwidth, 0);
				fillPath.lineTo(0, 0);
			}
		} else {
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
		}
		canvas.drawPath(fillPath, paintFill);

		paintOutline.setColor(colorMin);

		if (isShowMinMax()) {
			float f;
			// int _t, _l, _w, _h;
			outlinePath.reset();

			if (invert) {
				if (height > width) {
					//
					f = height * (min);
					outlinePath.reset();
					outlinePath.moveTo(0, f);
					outlinePath.lineTo(width, f);
					canvas.drawPath(outlinePath, paintOutline);

					outlinePath.reset();
					f = height * (max);
					outlinePath.moveTo(0, f);
					outlinePath.lineTo(width, f);
					canvas.drawPath(outlinePath, paintOutline);

				} else {

					f = width * max;
					outlinePath.reset();
					outlinePath.moveTo(f, 0);
					outlinePath.lineTo(f, height);
					canvas.drawPath(outlinePath, paintOutline);

					f = width * min;
					outlinePath.reset();
					outlinePath.moveTo(f, 0);
					outlinePath.lineTo(f, height);
					canvas.drawPath(outlinePath, paintOutline);
				}
			} else {
				if (height > width) {
					f = height * (1 - min);

					outlinePath.reset();
					outlinePath.moveTo(0, f);
					outlinePath.lineTo(width, f);
					canvas.drawPath(outlinePath, paintOutline);

					outlinePath.reset();
					f = height * (1 - max);
					outlinePath.moveTo(0, f);
					outlinePath.lineTo(width, f);
					canvas.drawPath(outlinePath, paintOutline);

				} else {

					f = width * min;
					outlinePath.reset();
					outlinePath.moveTo(f, 0);
					outlinePath.lineTo(f, height);
					canvas.drawPath(outlinePath, paintOutline);

					f = width * max;
					outlinePath.reset();
					outlinePath.moveTo(f, 0);
					outlinePath.lineTo(f, height);
					canvas.drawPath(outlinePath, paintOutline);
				}
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
		// Log.d("fillbar", "Min: " + String.valueOf(min));
		// Log.d("fillbar", "Max: " + String.valueOf(max));
		invalidate();
	}

	public void setup(int max, int min) {
		this.val_max = max;
		this.val_min = min;
	}

	public void setValue(int value) {
		this.setPercentage((value - val_min) / ((float) (val_max - val_min)));
	}

	public boolean isShowMinMax() {
		return showMinMax;
	}

	public void invertBar(boolean inv) {
		invert = inv;
	}

	public void setShowMinMax(boolean showMinMax) {
		this.showMinMax = showMinMax;
		if (showMinMax) {
			min = 0.5f;
			max = 0.5f;
		}
		invalidate();
	}

	public float getMin() {
		return this.min;
	}

	public float getMax() {
		return this.max;
	}

	public int getMinValue() {
		return val_min + (int) (getMin() * ((float) (val_max - val_min)));
	}

	public int getMaxValue() {
		return val_min + (int) (getMax() * ((float) (val_max - val_min)));
	}

	public int getColorOutline() {
		return colorOutline;
	}

	public void setColorOutline(int colorOutline) {
		this.colorOutline = colorOutline;
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
