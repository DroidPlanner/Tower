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