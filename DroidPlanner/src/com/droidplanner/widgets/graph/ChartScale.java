package com.droidplanner.widgets.graph;


public class ChartScale {
	private double range;
	private double pan;
	private double min;
	private double max;

	public ChartScale(double range, double min, double max) {
		this.range = range;
		this.pan = 0;
		this.min = min;
		this.max = max;
	}

	void scale(float scale) {
		range /= scale;
		range = Math.max(min, Math.min(range, max));
	}

	public double getRange() {
		return range;
	}
	
	public double getMax() {
		return max;
	}

	public void setMax(double d) {
		max = d;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getOffset() {
		return pan;
	}

	public void setPan(double i) {
		pan = i;		
	}

	public void pan(float distanceY) {
		pan+=distanceY;		
	}

	public double getGridSize() {
		return range/10;
	}
}