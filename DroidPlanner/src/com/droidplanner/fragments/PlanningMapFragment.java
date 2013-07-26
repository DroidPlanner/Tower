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
import com.droidplanner.drone.variables.Home;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.markers.MarkerManager;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.polygon.Polygon;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

@SuppressLint("UseSparseArrays")
public class PlanningMapFragment extends OfflineMapFragment implements
		OnMapLongClickListener, OnMarkerDragListener {

	public enum modes {
		MISSION, POLYGON;
	}

	public GoogleMap mMap;

	private MarkerManager markers;

	private OnMapInteractionListener mListener;

	public modes mode = modes.MISSION;

	public Polygon polygon;

	private Polyline polygonLine;

	private Polyline missionLine;

	public interface OnMapInteractionListener {

		public void onAddPoint(LatLng point);

		public void onMoveHome(LatLng coord);

		public void onMoveWaypoint(waypoint waypoint, LatLng latLng);

		public void onMovePolygonPoint(PolygonPoint source, LatLng newCoord);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		mMap = getMap();
		mMap.setOnMarkerDragListener(this);
		mMap.setOnMapLongClickListener(this);

		markers = new MarkerManager(mMap);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (OnMapInteractionListener) activity;
	}

	public void update(Drone drone, Polygon polygon) {
		markers.clear();

		markers.updateMarker(drone.mission.getHome(),true);
		markers.updateMarkers(drone.mission.getWaypoints(),true);
		markers.updateMarkers(polygon.getPolygonPoints(),true);

		clearPolylines();

		polygonLine = mMap.addPolyline(getPolygonPath(polygon));
		missionLine = mMap.addPolyline(getMissionPath(drone));

	}

	private void clearPolylines() {
		if (polygonLine != null) {
			polygonLine.remove();
		}
		if (missionLine != null) {
			missionLine.remove();
		}
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
		MarkerSource source = markers.getSourceFromMarker(marker);
		checkForHomeMarker(source, marker);
		checkForWaypointMarker(source, marker);
		checkForPolygonMarker(source, marker);
	}

	private void checkForHomeMarker(MarkerSource source, Marker marker) {
		if (Home.class.isInstance(source)) {
			mListener.onMoveHome(marker.getPosition());
		}
	}

	private void checkForWaypointMarker(MarkerSource source, Marker marker) {
		if (waypoint.class.isInstance(source)) {
			mListener.onMoveWaypoint((waypoint) source, marker.getPosition());
		}
	}

	private void checkForPolygonMarker(MarkerSource source, Marker marker) {
		if (PolygonPoint.class.isInstance(source)) {
			mListener.onMovePolygonPoint((PolygonPoint) source,
					marker.getPosition());
		}
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
