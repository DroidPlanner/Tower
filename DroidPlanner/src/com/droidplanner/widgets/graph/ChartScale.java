package com.droidplanner.widgets.graph;

import android.content.Context;
import android.view.ScaleGestureDetector;

public class ChartScale {
	public interface OnScaleListner{
		public void onScaleListner();
	}
	
	private OnScaleListner listner;

	// range values to display
	private double range = 180;
	// minimal range
	private double min = 10;
	// maximal range
	private double max = 180;

	protected ScaleGestureDetector scaleDetector;

	public ChartScale(Context context, OnScaleListner listner) {
		scaleDetector = new ScaleGestureDetector(context, new ChartScaleListener());
		this.listner = listner;		
	}

	public double getRange() {
		return range;
	}

	class ChartScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			range /= detector.getScaleFactor();

			range = Math.max(min, Math.min(range, max));
			listner.onScaleListner();
			return true;
		}
	}
}