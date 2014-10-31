package org.droidplanner.android.fragments;

import java.util.List;

import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.proxy.mission.item.markers.MissionItemMarkerInfo;
import org.droidplanner.android.proxy.mission.item.markers.SurveyMarkerInfoProvider;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

@SuppressLint("UseSparseArrays")
public class EditorMapFragment extends DroneMap implements DPMap.OnMapLongClickListener,
		DPMap.OnMarkerDragListener, DPMap.OnMapClickListener, DPMap.OnMarkerClickListener {

	private OnEditorInteraction editorListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

		mMapFragment.setOnMarkerDragListener(this);
		mMapFragment.setOnMarkerClickListener(this);
		mMapFragment.setOnMapClickListener(this);
		mMapFragment.setOnMapLongClickListener(this);

		return view;
	}

	@Override
	public void onMapLongClick(Coord2D point) {
	}

	@Override
	public void onMarkerDrag(MarkerInfo markerInfo) {
		checkForWaypointMarkerMoving(markerInfo);
	}

	@Override
	public void onMarkerDragStart(MarkerInfo markerInfo) {
		checkForWaypointMarkerMoving(markerInfo);
	}

	private void checkForWaypointMarkerMoving(MarkerInfo markerInfo) {
		if (SpatialCoordItem.class.isInstance(markerInfo)) {
			Coord2D position = markerInfo.getPosition();

			// update marker source
			SpatialCoordItem waypoint = (SpatialCoordItem) markerInfo;
			waypoint.setPosition(position);

			// update flight path
			mMapFragment.updateMissionPath(missionProxy);
		}
	}

	@Override
	public void onMarkerDragEnd(MarkerInfo markerInfo) {
		checkForWaypointMarker(markerInfo);
	}

	private void checkForWaypointMarker(MarkerInfo markerInfo) {
		if (!(markerInfo instanceof SurveyMarkerInfoProvider)
				&& (markerInfo instanceof MissionItemMarkerInfo)) {
			missionProxy.move(((MissionItemMarkerInfo) markerInfo).getMarkerOrigin(),
					markerInfo.getPosition());
		}
	}

    @Override
    public void onStart(){
        super.onStart();
        zoomToFit();
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
		if (target == AutoPanMode.DISABLED)
			return true;

		Toast.makeText(getActivity(), "Auto pan is not supported on this map.", Toast.LENGTH_LONG)
				.show();
		return false;
	}

	@Override
	public boolean onMarkerClick(MarkerInfo info) {
		if (info instanceof MissionItemMarkerInfo) {
			editorListener.onItemClick(((MissionItemMarkerInfo) info).getMarkerOrigin(), false);
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
		// get visible mission coords
		final List<Coord2D> visibleCoords = missionProxy.getVisibleCoords();

		// add home coord if visible
		final Coord2D homeCoord = drone.getHome().getCoord();
		if (homeCoord != null && !homeCoord.isEmpty())
			visibleCoords.add(homeCoord);

        zoomToFit(visibleCoords);
	}

    public void zoomToFit(List<Coord2D> itemsToFit){
        if(!itemsToFit.isEmpty()){
            mMapFragment.zoomToFit(itemsToFit);
        }
    }

}
