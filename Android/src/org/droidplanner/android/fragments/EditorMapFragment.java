package org.droidplanner.android.fragments;

import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.maps.fragments.DroneMap;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.android.graphic.map.MarkerManager.MarkerSource;
import org.droidplanner.android.mission.item.markers.MissionItemMarkerSource;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;

import android.annotation.SuppressLint;
import android.app.Activity;
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
		OnMarkerClickListener {

//	public MapPath polygonPath;
//	public CameraGroundOverlays cameraOverlays;
	private OnEditorInteraction editorListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

        mMapFragment.setOnMarkerDragListener(this);
        mMapFragment.setOnMarkerClickListener(this);
        mMapFragment.setOnMapClickListener(this);
        mMapFragment.setOnMapLongClickListener(this);

        //TODO: figure out if it's still needed
//		polygonPath = new MapPath(mMap, Color.BLACK, getResources());
//		cameraOverlays = new CameraGroundOverlays(mMap);

		return view;
	}

	@Override
	public void onMapLongClick(LatLng point) {
		// mListener.onAddPoint(point);
	}

	@Override
	public void onMarkerDrag(Marker marker) {
		MarkerSource source = mMapFragment.getMarkerSource(marker);
		checkForWaypointMarkerMoving(source, marker, true);
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		MarkerSource source = mMapFragment.getMarkerSource(marker);
		checkForWaypointMarkerMoving(source, marker, false);
	}

	private void checkForWaypointMarkerMoving(MarkerSource source,
			Marker marker, boolean dragging) {
		if (SpatialCoordItem.class.isInstance(source)) {
			LatLng position = marker.getPosition();

			// update marker source
			SpatialCoordItem waypoint = (SpatialCoordItem) source;
			waypoint.setPosition(DroneHelper.LatLngToCoord(position));

			/*
			 * // update info window if(dragging)
			 * waypoint.updateDistanceFromPrevPoint(); else
			 * waypoint.setPrevPoint(mission.getWaypoints());
			 * updateInfoWindow(waypoint, marker);
			 */

			// update flight path
            mMapFragment.updateMissionPath(missionRender);
		}
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		MarkerSource source = mMapFragment.getMarkerSource(marker);
		checkForWaypointMarker(source, marker);
		checkForPolygonMarker(source, marker);
	}

	private void checkForWaypointMarker(MarkerSource source, Marker marker) {
		if (SpatialCoordItem.class.isInstance(source)) {
			// mListener.onMoveWaypoint((SpatialCoordItem) source,
			// marker.getPosition());
		}
	}

	private void checkForPolygonMarker(MarkerSource source, Marker marker) {
		/*
		 * if (PolygonPoint.class.isInstance(source)) {
		 * Listener.onMovePolygonPoint((PolygonPoint)
		 * source,marker.getPosition()); }
		 */
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
		MarkerSource source = mMapFragment.getMarkerSource(marker);
		if (source instanceof MissionItemMarkerSource) {
			editorListener.onItemClick(((MissionItemMarkerSource) source).getMarkerOrigin());
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected boolean isMissionDraggable() {
		return true;
	}

}
