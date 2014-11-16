package org.droidplanner.android.fragments;

import java.util.ArrayList;
import java.util.List;


import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.view.MotionEvent;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.util.MathUtils;


public class PathGesture implements OnGestureListener {

	private static final int TOLERANCE = 15;
	private static final int STROKE_WIDTH = 3;

	public interface OnPathFinishedListener {

		void onPathFinished(List<LatLong> path);
	}

	public double toleranceInPixels;
	public GestureOverlayView view;
	public OnPathFinishedListener listener;

	public PathGesture(GestureOverlayView view) {
		this.view = view;
		this.view.addOnGestureListener(this);
		this.view.setEnabled(false);
		this.view.setGestureStrokeWidth(scaleDpToPixels(STROKE_WIDTH));
		toleranceInPixels = scaleDpToPixels(TOLERANCE);
	}

	private int scaleDpToPixels(double value) {
		final float scale = view.getResources().getDisplayMetrics().density;
		return (int) Math.round(value * scale);
	}

	public void enableGestureDetection() {
		view.setEnabled(true);
	}

	public void setOnPathFinishedListener(OnPathFinishedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onGestureEnded(GestureOverlayView arg0, MotionEvent arg1) {
		view.setEnabled(false);
		List<LatLong> path = decodeGesture();
		if (path.size() > 1) {
			path = MathUtils.simplify(path, toleranceInPixels);
		}
		listener.onPathFinished(path);
	}

	private List<LatLong> decodeGesture() {
		List<LatLong> path = new ArrayList<LatLong>();
		float[] points = view.getGesture().getStrokes().get(0).points;
		for (int i = 0; i < points.length; i += 2) {
			path.add(new LatLong((int) points[i], (int) points[i + 1]));
		}
		return path;
	}

	@Override
	public void onGesture(GestureOverlayView arg0, MotionEvent arg1) {
	}

	@Override
	public void onGestureCancelled(GestureOverlayView arg0, MotionEvent arg1) {
	}

	@Override
	public void onGestureStarted(GestureOverlayView arg0, MotionEvent arg1) {
	}
}
