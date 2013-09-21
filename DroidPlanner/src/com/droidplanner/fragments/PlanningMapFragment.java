package com.droidplanner.fragments;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.dialogs.mission.DialogMissionFactory;
import com.droidplanner.drone.variables.Mission;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.helpers.DroneMap;
import com.droidplanner.fragments.helpers.MapPath;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.helpers.geoTools.GeoTools;
import com.droidplanner.polygon.Polygon;
import com.droidplanner.polygon.PolygonPoint;
import com.droidplanner.survey.SurveyData;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolygonOptions;

@SuppressLint("UseSparseArrays")
public class PlanningMapFragment extends DroneMap implements
		OnMapLongClickListener, OnMarkerDragListener, OnMapClickListener,
		OnMarkerClickListener {

	public OnMapInteractionListener mListener;
	private MapPath polygonPath;
	private Mission mission;

	private ArrayList<com.google.android.gms.maps.model.Polygon> cameraOverlays = new ArrayList<com.google.android.gms.maps.model.Polygon>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

		mMap.setOnMarkerDragListener(this);
		mMap.setOnMarkerClickListener(this);
		mMap.setOnMapClickListener(this);
		mMap.setOnMapLongClickListener(this);
		polygonPath = new MapPath(mMap, Color.BLACK, 2);

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
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
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

	public void addCameraFootPrints(List<LatLng> cameraLocations,
			SurveyData surveyData) {
		for (com.google.android.gms.maps.model.Polygon overlay : cameraOverlays) {
			overlay.remove();
		}
		cameraOverlays.clear();
		for (LatLng latLng : cameraLocations) {
			addOneFootprint(latLng, surveyData);
		}
	}

	private void addOneFootprint(LatLng latLng, SurveyData surveyData) {
		double lng = surveyData.getLateralFootPrint().valueInMeters();
		double lateral = surveyData.getLongitudinalFootPrint().valueInMeters();
		double halfDiag = Math.hypot(lng, lateral) / 2;
		double angle = Math.toDegrees(Math.tan(lng / lateral));
		cameraOverlays.add(mMap.addPolygon(new PolygonOptions()
				.add(GeoTools.newCoordFromBearingAndDistance(latLng,
						surveyData.getAngle() - angle, halfDiag),
						GeoTools.newCoordFromBearingAndDistance(latLng,
								surveyData.getAngle() + angle, halfDiag),
						GeoTools.newCoordFromBearingAndDistance(latLng,
								surveyData.getAngle() + 180 - angle, halfDiag),
						GeoTools.newCoordFromBearingAndDistance(latLng,
								surveyData.getAngle() + 180 + angle, halfDiag))
				.fillColor(Color.argb(40, 0, 0, 127)).strokeWidth(1)
				.strokeColor(Color.argb(127, 0, 0, 255))));

	}

}
