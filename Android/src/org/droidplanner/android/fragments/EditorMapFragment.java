package org.droidplanner.android.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.proxy.mission.item.markers.MissionItemMarkerInfo;
import org.droidplanner.android.proxy.mission.item.markers.SurveyMarkerInfoProvider;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;

@SuppressLint("UseSparseArrays")
public class EditorMapFragment extends DroneMap implements
        DPMap.OnMapLongClickListener, DPMap.OnMarkerDragListener, DPMap.OnMapClickListener,
        DPMap.OnMarkerClickListener {

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
    public void onMapLongClick(Coord2D point) {
        // mListener.onAddPoint(point);
    }

    @Override
    public void onMarkerDrag(MarkerInfo markerInfo) {
        checkForWaypointMarkerMoving(markerInfo, true);
    }

    @Override
    public void onMarkerDragStart(MarkerInfo markerInfo) {
        checkForWaypointMarkerMoving(markerInfo, false);
    }

    private void checkForWaypointMarkerMoving(MarkerInfo markerInfo, boolean dragging) {
        if (SpatialCoordItem.class.isInstance(markerInfo)) {
            Coord2D position = markerInfo.getPosition();

            // update marker source
            SpatialCoordItem waypoint = (SpatialCoordItem) markerInfo;
            waypoint.setPosition(position);

			/*
             * // update info window if(dragging)
			 * waypoint.updateDistanceFromPrevPoint(); else
			 * waypoint.setPrevPoint(mission.getWaypoints());
			 * updateInfoWindow(waypoint, marker);
			 */

            // update flight path
            mMapFragment.updateMissionPath(missionProxy);
        }
    }

    @Override
    public void onMarkerDragEnd(MarkerInfo markerInfo) {
        checkForWaypointMarker(markerInfo);
        checkForPolygonMarker(markerInfo);
    }

    private void checkForWaypointMarker(MarkerInfo markerInfo) {
        if(!(markerInfo instanceof SurveyMarkerInfoProvider) && (markerInfo instanceof
                MissionItemMarkerInfo)){
            missionProxy.move(((MissionItemMarkerInfo)markerInfo).getMarkerOrigin(),
                    markerInfo.getPosition());
        }
    }

    private void checkForPolygonMarker(MarkerInfo info) {
		/*
		 * if (PolygonPoint.class.isInstance(info)) {
		 * Listener.onMovePolygonPoint((PolygonPoint)
		 * info,marker.getPosition()); }
		 */
    }

    @Override
    public void onMapClick(Coord2D point) {
        editorListener.onMapClick(point);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        editorListener = (OnEditorInteraction) activity;
    }

    @Override
    public boolean setAutoPanMode(AutoPanMode target) {
        if(target == AutoPanMode.DISABLED)
            return true;

        Toast.makeText(getActivity(), "Auto pan is not supported on this map.",
                Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public boolean onMarkerClick(MarkerInfo info) {
        if (info instanceof MissionItemMarkerInfo) {
            editorListener.onItemClick(((MissionItemMarkerInfo) info).getMarkerOrigin());
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
