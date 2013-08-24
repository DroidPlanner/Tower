package com.droidplanner.widgets.graph;

import com.droidplanner.widgets.graph.ChartScaleHandler.ChartScaleListener;

public class ChartScale {
	public double range;
	public double min;
	public double max;

	public ChartScale(double range, double min, double max) {
		this.range = range;
		this.min = min;
		this.max = max;
	}

	void scale(float scale, ChartScaleListener chartScaleListener) {
		range /= scale;
		range = Math.max(min, Math.min(range, max));
	}

	public double getRange(ChartScaleHandler chartScaleHandler) {
		return range;
	}
}