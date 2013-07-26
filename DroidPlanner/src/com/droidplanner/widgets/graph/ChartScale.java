package com.droidplanner.widgets.graph;

import android.content.Context;
import android.view.ScaleGestureDetector;

public class ChartScale {
	public ScaleGestureDetector scaleDetector;

	// range values to display
	double range = 180;
	// minimal range
	double min = 0.1;
	// maximal range
	double max = 180;

	public ChartScale(Context context) {
		scaleDetector = new ScaleGestureDetector(context, new ChartScaleListener());
	}

	class ChartScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			range /= detector.getScaleFactor();

			range = Math.max(min, Math.min(range, max));

			return true;
		}
	}
}