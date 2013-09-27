package com.droidplanner.fragments;

import java.util.ArrayList;
import java.util.List;

import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Point;
import android.view.MotionEvent;

import com.droidplanner.helpers.geoTools.Simplify;

public class PathGesture implements OnGestureListener {

	private static final int TOLERANCE = 15;
	private static final int STROKE_WIDTH = 3;

	public interface OnPathFinishedListner {

		void onPathFinished(List<Point> path);
	}

	public double toleranceInPixels;
	public GestureOverlayView view;
	public OnPathFinishedListner listner;

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

	public void setOnPathFinishedListner(OnPathFinishedListner listner) {
		this.listner = listner;
	}

	@Override
	public void onGestureEnded(GestureOverlayView arg0, MotionEvent arg1) {
		view.setEnabled(false);
		List<Point> path = decodeGesture();
		if (path.size() > 1) {
			path = Simplify.simplify(path, toleranceInPixels);
		}
		listner.onPathFinished(path);
	}

	private List<Point> decodeGesture() {
		List<Point> path = new ArrayList<Point>();
		extractPathFromGesture(path);
		return path;
	}

	private void extractPathFromGesture(List<Point> path) {
		float[] points = view.getGesture().getStrokes().get(0).points;
		for (int i = 0; i < points.length; i += 2) {
			path.add(new Point((int) points[i], (int) points[i + 1]));
		}
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