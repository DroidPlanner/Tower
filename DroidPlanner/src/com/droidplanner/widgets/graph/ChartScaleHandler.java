package com.droidplanner.widgets.graph;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.droidplanner.widgets.graph.series.StaticSeries;

public class ChartScaleHandler {
	public interface OnScaleListner {
		public void onScaleListner();
	}

	private OnScaleListner listner;
	private ScaleGestureDetector zoomDetector;
	private GestureDetector panDetector;
	public ChartScale y = new ChartScale(180, 10, 180);
	public ChartScale x = new ChartScale(400, 100, 800);

	public ChartScaleHandler(Context context, OnScaleListner listner) {
		zoomDetector = new ScaleGestureDetector(context,
				new ChartZoomListener());
		panDetector = new GestureDetector(context, new ChartPanListener());
		this.listner = listner;
	}

	public void autoScale(StaticSeries dataSeries) {
		autoScale(dataSeries, 1);
	}

	public void autoScale(StaticSeries dataSeries, double overScale) {
		y.setMax(dataSeries.getMaxValue() * overScale);
		y.setRange(y.getMax());
	}

	public boolean onTouchEvent(MotionEvent ev) {
		zoomDetector.onTouchEvent(ev);
		panDetector.onTouchEvent(ev);
		return true;
	}

	class ChartZoomListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if (detector.getCurrentSpanY() > detector.getCurrentSpanX()) {
				y.scale(detector.getScaleFactor());
			} else {
				x.scale(detector.getScaleFactor());
			}
			listner.onScaleListner();
			Log.d("", "Scale" + detector.getScaleFactor());
			return true;
		}
	}

	class ChartPanListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			y.pan(distanceY);
			listner.onScaleListner();
			Log.d("", "X:" + distanceX + " Y:" + distanceY);
			return true;
		}
	}

}