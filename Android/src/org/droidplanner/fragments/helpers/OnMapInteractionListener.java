package org.droidplanner.fragments.helpers;

import org.droidplanner.mission.MissionItem;
import org.droidplanner.mission.waypoints.SpatialCoordItem;

import com.google.android.gms.maps.model.LatLng;

public interface OnMapInteractionListener {

	public void onAddPoint(LatLng point);

	public void onMoveHome(LatLng coord);

	public void onMoveWaypoint(SpatialCoordItem waypoint, LatLng latLng);

	public void onMovingWaypoint(SpatialCoordItem source, LatLng latLng);

	public void onMapClick(LatLng point);

	public boolean onMarkerClick(MissionItem missionItem);
}