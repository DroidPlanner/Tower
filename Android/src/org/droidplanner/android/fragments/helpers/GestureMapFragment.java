package org.droidplanner.android.fragments.helpers;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.R;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.Simplify;

import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class GestureMapFragment extends Fragment implements OnGestureListener {
	private static final int TOLERANCE = 15;
	private static final int STROKE_WIDTH = 3;

	private double toleranceInPixels;

	public interface OnPathFinishedListener {

		void onPathFinished(List<Coord2D> path);
	}

	private GestureOverlayView overlay;
	private OnPathFinishedListener listener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_gesture_map, container,
				false);
		overlay = (GestureOverlayView) view.findViewById(R.id.overlay1);
		overlay.addOnGestureListener(this);
		overlay.setEnabled(false);

		overlay.setGestureStrokeWidth(scaleDpToPixels(STROKE_WIDTH));
		toleranceInPixels = scaleDpToPixels(TOLERANCE);
		return view;
	}

	private int scaleDpToPixels(double value) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) Math.round(value * scale);
	}

	public void enableGestureDetection() {
		overlay.setEnabled(true);
	}

	public void disableGestureDetection() {
		overlay.setEnabled(false);
	}

	public void setOnPathFinishedListener(OnPathFinishedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onGestureEnded(GestureOverlayView arg0, MotionEvent arg1) {
		overlay.setEnabled(false);
		List<Coord2D> path = decodeGesture();
		if (path.size() > 1) {
			path = Simplify.simplify(path, toleranceInPixels);
		}
		listener.onPathFinished(path);
	}

	private List<Coord2D> decodeGesture() {
		List<Coord2D> path = new ArrayList<Coord2D>();
		extractPathFromGesture(path);
		return path;
	}

	private void extractPathFromGesture(List<Coord2D> path) {
		float[] points = overlay.getGesture().getStrokes().get(0).points;
		for (int i = 0; i < points.length; i += 2) {
			path.add(new Coord2D((int) points[i], (int) points[i + 1]));
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
