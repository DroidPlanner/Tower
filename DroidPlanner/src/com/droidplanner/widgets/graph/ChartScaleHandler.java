package com.droidplanner.widgets.graph;

import com.droidplanner.widgets.graph.series.StaticSeries;

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

	public void autoScale(StaticSeries dataSeries) {
		autoScale(dataSeries, 1);
	}

	public void autoScale(StaticSeries dataSeries, double overScale) {
		scaleY.max = dataSeries.getMaxValue();
		scaleY.range = scaleY.max*overScale;
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