package com.droidplanner.fragments.helpers;

import com.droidplanner.drone.variables.mission.waypoints.SpatialCoordItem;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.model.LatLng;

public interface OnMapInteractionListener {

	public void onAddPoint(LatLng point);

	public void onMoveHome(LatLng coord);

	public void onMoveWaypoint(SpatialCoordItem waypoint, LatLng latLng);

	public void onMovingWaypoint(SpatialCoordItem source, LatLng latLng);

	public void onMovePolygonPoint(PolygonPoint source, LatLng newCoord);

	public void onMapClick(LatLng point);

	public boolean onMarkerClick(SpatialCoordItem wp);
}