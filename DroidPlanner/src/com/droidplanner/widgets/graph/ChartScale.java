package com.droidplanner.widgets.graph;

import android.content.Context;
import android.view.ScaleGestureDetector;

public class ChartScale {
	public interface OnScaleListner{
		public void onScaleListner();
	}
	
	public ScaleGestureDetector scaleDetector;
	private OnScaleListner listner;

	// range values to display
	double range = 180;
	// minimal range
	double min = 10;
	// maximal range
	double max = 180;

	public ChartScale(Context context, OnScaleListner listner) {
		scaleDetector = new ScaleGestureDetector(context, new ChartScaleListener());
		this.listner = listner;		
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