package org.droidplanner.android.widgets.graph;

import android.graphics.Paint;

public class ChartSeries {

	private boolean enabled = false;
	public double[] data;
	public int newestData = 0;
	private Paint paint = new Paint();

	public ChartSeries(int bufferSize) {
		this.data = new double[bufferSize];
	}

	public void newData(double d) {
		if (data.length > 0) {
			newestData = (newestData + 1) % data.length;
			data[newestData] = d;
		}
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
