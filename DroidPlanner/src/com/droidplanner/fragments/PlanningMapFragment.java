package com.droidplanner.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.droidplanner.R.string;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.markers.MarkerManager;
import com.droidplanner.polygon.Polygon;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

@SuppressLint("UseSparseArrays")
public class PlanningMapFragment extends OfflineMapFragment implements
		OnMapLongClickListener, OnMarkerDragListener {

	public enum modes {
		MISSION, POLYGON;
	}

	public GoogleMap mMap;

	private MarkerManager waypointMarkers;

	private OnMapInteractionListener mListener;

	public modes mode = modes.MISSION;

	public Polygon polygon;

	public interface OnMapInteractionListener {

		public void onAddPoint(LatLng point);

		public void onMoveHome(LatLng coord);

		public void onMoveWaypoint(LatLng coord, int Number);

		public void onMovePolygonPoint(LatLng coord, int Number);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		mMap = getMap();
		mMap.setOnMarkerDragListener(this);
		mMap.setOnMapLongClickListener(this);

		waypointMarkers = new MarkerManager(mMap);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (OnMapInteractionListener) activity;
	}

	public void update(Drone drone, Polygon polygon) {
		clearMap();
		waypointMarkers.clear();

		waypointMarkers.updateMarker(drone.mission.getHome());
		waypointMarkers.updateMarkers(drone.mission.getWaypoints());
		waypointMarkers.updateMarkers(polygon.getPolygonPoints());

		mMap.addPolyline(getPolygonPath(polygon));
		mMap.addPolyline(getMissionPath(drone));

	}

	@Override
	public void onMapLongClick(LatLng point) {
		mListener.onAddPoint(point);
	}

	@Override
	public void onMarkerDrag(Marker marker) {
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		checkForHomeMarker(marker);
		checkForWaypointMarker(marker);
		checkForPolygonMarker(marker);
	}

	private void checkForHomeMarker(Marker marker) {
		// TODO reimplement this
		// if (homeMarker.isHomeMarker(marker)) {
		// mListener.onMoveHome(marker.getPosition());
		// }
	}

	private void checkForWaypointMarker(Marker marker) {
		// TODO reimplement this
		// MarkerSource source = waypointMarkers.getSourceFromMarker(marker);
		// Listener.onMoveWaypoint(, 0);
	}

	private void checkForPolygonMarker(Marker marker) {
		// TODO reimplement this
		/*
		 * if (polygonMarkers.containsValue(marker)) { int number = 0; for
		 * (HashMap.Entry<Integer, Marker> e : polygonMarkers.entrySet()) { if
		 * (marker.equals(e.getValue())) { number = e.getKey(); break; } }
		 * Log.d("MARK", "move polygon");
		 * mListener.onMovePolygonPoint(marker.getPosition(), number); }
		 */
	}

	public LatLng getMyLocation() {
		if (mMap.getMyLocation() != null) {
			return new LatLng(mMap.getMyLocation().getLatitude(), mMap
					.getMyLocation().getLongitude());
		} else {
			return null;
		}
	}

	private PolylineOptions getMissionPath(Drone drone) {
		PolylineOptions flightPath = new PolylineOptions();
		flightPath.color(Color.YELLOW).width(3);

		flightPath.add(drone.mission.getHome().getCoord());
		for (waypoint point : drone.mission.getWaypoints()) {
			flightPath.add(point.getCoord());
		}
		return flightPath;
	}

	public PolylineOptions getPolygonPath(Polygon poly) {
		PolylineOptions flightPath = new PolylineOptions();
		flightPath.color(Color.BLACK).width(2);

		for (LatLng point : poly.getLatLng()) {
			flightPath.add(point);
		}
		if (poly.getLatLng().size() > 2) {
			flightPath.add(poly.getLatLng().get(0));
		}

		return flightPath;
	}

	public void setMode(modes mode) {
		this.mode = mode;
		switch (mode) {
		default:
		case MISSION:
			Toast.makeText(getActivity(), string.exiting_polygon_mode,
					Toast.LENGTH_SHORT).show();
			break;
		case POLYGON:
			Toast.makeText(getActivity(), string.entering_polygon_mode,
					Toast.LENGTH_SHORT).show();
			break;
		}
	}
}
