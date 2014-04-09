package org.droidplanner.fragments;


import org.droidplanner.activities.helpers.OnEditorInteraction;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.drone.variables.mission.waypoints.SpatialCoordItem;
import org.droidplanner.fragments.helpers.CameraGroundOverlays;
import org.droidplanner.fragments.helpers.DroneMap;
import org.droidplanner.fragments.helpers.MapPath;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.polygon.Polygon;
import org.droidplanner.polygon.PolygonPoint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

@SuppressLint("UseSparseArrays")
public class EditorMapFragment extends DroneMap implements
		OnMapLongClickListener, OnMarkerDragListener, OnMapClickListener,
		OnMarkerClickListener{

	public MapPath polygonPath;

	public CameraGroundOverlays cameraOverlays;
	public Polygon polygon = new Polygon();
	private OnEditorInteraction editorListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

		mMap.setOnMarkerDragListener(this);
		mMap.setOnMarkerClickListener(this);
		mMap.setOnMapClickListener(this);
		mMap.setOnMapLongClickListener(this);
		polygonPath = new MapPath(mMap, Color.BLACK, getResources());
		cameraOverlays = new CameraGroundOverlays(mMap);

		return view;
	}

	@Override
	public void update() {
		super.update();
		markers.updateMarkers(polygon .getPolygonPoints(), true, context);
		polygonPath.update(polygon);
	}

	@Override
	public void onMapLongClick(LatLng point) {
		//mListener.onAddPoint(point);
	}

	@Override
	public void onMarkerDrag(Marker marker) {
		MarkerSource source = markers.getSourceFromMarker(marker);
		checkForWaypointMarkerMoving(source, marker, true);
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		MarkerSource source = markers.getSourceFromMarker(marker);
		checkForWaypointMarkerMoving(source, marker, false);
	}

	private void checkForWaypointMarkerMoving(MarkerSource source, Marker marker, boolean dragging) {
		if (SpatialCoordItem.class.isInstance(source)) {
			LatLng position = marker.getPosition();

			// update marker source
			SpatialCoordItem waypoint = (SpatialCoordItem) source;
			waypoint.setCoordinate(position);

			/*
			// update info window
			if(dragging)
				waypoint.updateDistanceFromPrevPoint();
			else
				waypoint.setPrevPoint(mission.getWaypoints());
			updateInfoWindow(waypoint, marker);
			 */

			// update flight path
			missionPath.update(mission);
		}
	}

	/*
	private void updateInfoWindow(GenericWaypoint waypoint, Marker marker) {
		marker.setTitle(waypoint.getNumber() + " " + waypoint.getCmd().getName());

		// display distance from last waypoint if available
		double distanceFromPrevPathPoint = waypoint.getDistanceFromPrevPoint();
		if(distanceFromPrevPathPoint != org.droidplanner.drone.variables.mission.waypoints.GenericWaypoint.UNKNOWN_DISTANCE)
			marker.setSnippet(String.format("%.0fm", distanceFromPrevPathPoint));

		marker.showInfoWindow();
	}
	*/

	@Override
	public void onMarkerDragEnd(Marker marker) {
		MarkerSource source = markers.getSourceFromMarker(marker);
		checkForWaypointMarker(source, marker);
		checkForPolygonMarker(source, marker);
	}

	private void checkForWaypointMarker(MarkerSource source, Marker marker) {
		if (SpatialCoordItem.class.isInstance(source)) {
			//mListener.onMoveWaypoint((SpatialCoordItem) source, marker.getPosition());
		}
	}

	private void checkForPolygonMarker(MarkerSource source, Marker marker) {
		if (PolygonPoint.class.isInstance(source)) {
			//mListener.onMovePolygonPoint((PolygonPoint) source,marker.getPosition());
		}
	}

	@Override
	public void onMapClick(LatLng point) {
		editorListener.onMapClick(point);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		editorListener = (OnEditorInteraction) activity;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		MarkerSource source = markers.getSourceFromMarker(marker);
		if (source instanceof MissionItem) {
			editorListener.onItemClick((MissionItem) source);
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected boolean isMissionDraggable() {
		return true;
	}

    public void zoomToFit() {
        zoomToExtents(mission.getVisibleCoordinates());
    }
}
