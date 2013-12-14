package com.droidplanner.widgets.graph.series;

import android.graphics.Color;
import android.graphics.Paint;

public abstract class ChartSeries {
	public abstract int getFirstIndex();

	private boolean enabled = false;
	public double[] data;
	private Paint paint = new Paint();

	public ChartSeries(int bufferSize) {
		this.data = new double[bufferSize];
		paint.setColor(Color.WHITE);
	}

	public double getMaxValue() {
		double higer = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < data.length; i++) {
			if (higer < data[i]) {
				higer = data[i];
			}
		}
		return higer;
	}

	public double getMinValue() {
		double lower = Double.POSITIVE_INFINITY;
		for (int i = 0; i < data.length; i++) {
			if (lower > data[i]) {
				lower = data[i];
			}
		}
		return lower;
	}
	
	public Paint getPaint() {
		return paint;
	}

	public void setColor(int color) {
		paint.setColor(color);
	}

	public int getColor() {
		return paint.getColor();
	}

	public void enable() {
		enabled = true;
	}

	public void disable() {
		enabled = false;
	}

	public boolean isActive() {
		return enabled;
	}

}