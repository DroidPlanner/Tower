package com.droidplanner.fragments.helpers;

import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.droidplanner.fragments.PlanningMapFragment;
import com.droidplanner.fragments.PlanningMapFragment.modes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

/**
 * Original in
 * http://stackoverflow.com/questions/13722869/how-to-handle-ontouch-
 * event-for-map-in-google-map-api-v2
 * 
 */
public class TouchableWrapper extends FrameLayout {
	private PlanningMapFragment fragment;
	private GoogleMap map;

	public TouchableWrapper(Context context) {
		super(context);
	}

	public void addParentFragment(PlanningMapFragment planningMapFragment) {
		this.fragment = planningMapFragment;
		map = fragment.mMap;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (fragment.mode == modes.PATH) {
				fragment.mListener.onPathStarted();
			}
		case MotionEvent.ACTION_MOVE:
			if (fragment.mode == modes.PATH) {
				Point point = new Point((int) event.getX(), (int) event.getY());
				LatLng coord = map.getProjection().fromScreenLocation(point);
				fragment.mListener.onAddPoint(coord);
				return true;
			}
		case MotionEvent.ACTION_UP:
			fragment.mListener.onPathFinished();
			return true;
		}
			
		return super.dispatchTouchEvent(event);
	}

}