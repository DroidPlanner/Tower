package com.droidplanner.fragments;


import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.dialogs.mission.DialogMissionFactory;
import com.droidplanner.drone.variables.Mission;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.PathGesture.OnPathFinishedListner;
import com.droidplanner.fragments.helpers.CameraGroundOverlays;
import com.droidplanner.fragments.helpers.DroneMap;
import com.droidplanner.fragments.helpers.MapPath;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.fragments.markers.MarkerManager;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.polygon.Polygon;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

@SuppressLint("UseSparseArrays")
public class PlanningMapFragment extends DroneMap implements
		OnMapLongClickListener, OnMarkerDragListener, OnMapClickListener,
		OnMarkerClickListener, OnPathFinishedListner {

	public OnMapInteractionListener mListener;
	private MapPath polygonPath;
	private Mission mission;

	public CameraGroundOverlays cameraOverlays;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

		mMap.setOnMarkerDragListener(this);
		mMap.setOnMarkerClickListener(this);
		mMap.setOnMapClickListener(this);
		mMap.setOnMapLongClickListener(this);
		polygonPath = new MapPath(mMap, Color.BLACK, 2);
		cameraOverlays = new CameraGroundOverlays(mMap);

		return view;
	}

	public void update(Polygon polygon) {
		markers.clear();

		Context context = getActivity().getApplicationContext();
		markers.updateMarker(mission.getHome(), false, context);
		markers.updateMarkers(mission.getWaypoints(), true, context);
		markers.updateMarkers(polygon.getPolygonPoints(), true, context);

		polygonPath.update(polygon);
		missionPath.update(mission);
	}

	@Override
	public void onMapLongClick(LatLng point) {
		mListener.onAddPoint(point);
	}

	@Override
	public void onMarkerDrag(Marker marker) {
		MarkerSource source = markers.getSourceFromMarker(marker);
		checkForWaypointMarkerMoving(source, marker);
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		MarkerSource source = markers.getSourceFromMarker(marker);
		checkForWaypointMarkerMoving(source, marker);
	}

	private void checkForWaypointMarkerMoving(MarkerSource source, Marker marker)
	{
		if (waypoint.class.isInstance(source)) {
			// update marker source and flight path
			waypoint waypoint = (waypoint) source;
			waypoint.setCoord(marker.getPosition());
			missionPath.update(mission);

			mListener.onMovingWaypoint(waypoint, marker.getPosition());
		}
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		MarkerSource source = markers.getSourceFromMarker(marker);
		checkForWaypointMarker(source, marker);
		checkForPolygonMarker(source, marker);
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

	@Override
	public void onMapClick(LatLng point) {
		mListener.onMapClick(point);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (OnMapInteractionListener) activity;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		MarkerSource source = markers.getSourceFromMarker(marker);
		if (source instanceof waypoint) {
			DialogMissionFactory.getDialog((waypoint) source,
					this.getActivity(), mission);
			return true;
		} else {
			return false;
		}
	}

	public void setMission(Mission mission) {
		this.mission = mission;
	}

	@Override
	public void onPathFinished(List<Point> path) {
		// TODO Auto-generated method stub
		
	}

}
