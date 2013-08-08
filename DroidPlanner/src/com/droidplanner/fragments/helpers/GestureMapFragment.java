package com.droidplanner.fragments.helpers;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.R;

public class GestureMapFragment extends Fragment implements OnGestureListener {
	private static final int TOLERANCE = 10;

	public interface OnPathFinishedListner {

		void onPathFinished(List<Point> path);
	}

	private GestureOverlayView overlay;
	private OnPathFinishedListner listner;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.gesture_map_fragment, container,
				false);
		overlay = (GestureOverlayView) view.findViewById(R.id.overlay1);
		overlay.addOnGestureListener(this);
		overlay.setEnabled(false);
		return view;
	}

	public void enableGestureDetection() {
		overlay.setEnabled(true);
	}

	public void setOnPathFinishedListner(OnPathFinishedListner listner) {
		this.listner = listner;
	}

	@Override
	public void onGestureEnded(GestureOverlayView arg0, MotionEvent arg1) {
		overlay.setEnabled(false);
		List<Point> path = decodeGesture();
		if (path.size() > 1) {
			path = Simplify.simplify(path, TOLERANCE);
		}
		listner.onPathFinished(path);
	}

	private List<Point> decodeGesture() {
		List<Point> path = new ArrayList<Point>();
		extractPathFromGesture(path);		
		return path;
	}

	private void extractPathFromGesture(List<Point> path) {
		float[] points = overlay.getGesture().getStrokes().get(0).points;
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
