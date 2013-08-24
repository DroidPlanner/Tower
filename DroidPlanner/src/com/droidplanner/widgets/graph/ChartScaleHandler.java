package com.droidplanner.widgets.graph;

import android.content.Context;
import android.view.ScaleGestureDetector;

public class ChartScaleHandler {
	public interface OnScaleListner {
		public void onScaleListner();
	}

	private OnScaleListner listner;
	protected ScaleGestureDetector scaleDetector;
	public ChartScale scaleY = new ChartScale(180, 10, 180);

	public ChartScaleHandler(Context context, OnScaleListner listner) {
		scaleDetector = new ScaleGestureDetector(context,
				new ChartScaleListener());
		this.listner = listner;
	}

	class ChartScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float scale = detector.getCurrentSpanY()/detector.getPreviousSpanY();
			scaleY.scale(scale, ChartScaleListener.this);
			listner.onScaleListner();
			return true;
		}
	}
}