package org.droidplanner.android.widgets.graph;

import android.content.Context;
import android.view.ScaleGestureDetector;

public class ChartScale {
	public interface OnScaleListener {
		public void onScaleListener();
	}

	private OnScaleListener listener;

	// range values to display
	private double range = 45;
	// minimal range
	private double min = 10;
	// maximal range
	private double max = 45;

	protected ScaleGestureDetector scaleDetector;

	public ChartScale(Context context, OnScaleListener listener) {
		scaleDetector = new ScaleGestureDetector(context,
				new ChartScaleListener());
		this.listener = listener;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range, double min, double max) {
		this.range = range;
		this.min = min;
		this.max = max;
	}

	class ChartScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			range /= detector.getScaleFactor();

			range = Math.max(min, Math.min(range, max));
			listener.onScaleListener();
			return true;
		}
	}
}
