package org.droidplanner.fragments.helpers;

import org.droidplanner.drone.variables.missionD.MissionItemD;
import org.droidplanner.drone.variables.missionD.waypoints.SpatialCoordItemD;
import org.droidplanner.polygon.PolygonPoint;

import com.google.android.gms.maps.model.LatLng;

public interface OnMapInteractionListener {

	public void onAddPoint(LatLng point);

	public void onMoveHome(LatLng coord);

	public void onMoveWaypoint(SpatialCoordItemD waypoint, LatLng latLng);

	public void onMovingWaypoint(SpatialCoordItemD source, LatLng latLng);

	public void onMovePolygonPoint(PolygonPoint source, LatLng newCoord);

	public void onMapClick(LatLng point);

	public boolean onMarkerClick(MissionItemD missionItem);
}