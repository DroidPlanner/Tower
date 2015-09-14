package org.droidplanner.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.property.Home;

import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.proxy.mission.item.markers.MissionItemMarkerInfo;
import org.droidplanner.android.proxy.mission.item.markers.PolygonMarkerInfo;
import org.droidplanner.android.utils.prefs.AutoPanMode;

import java.util.ArrayList;
import java.util.List;

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
	public void onMapLongClick(LatLong point) {
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
		if (markerInfo instanceof MissionItem.SpatialItem) {
			LatLong position = markerInfo.getPosition();

			// update marker source
			MissionItem.SpatialItem waypoint = (MissionItem.SpatialItem) markerInfo;
            LatLong waypointPosition = waypoint.getCoordinate();
            waypointPosition.setLatitude(position.getLatitude());
            waypointPosition.setLongitude(position.getLongitude());

			// update flight path
			mMapFragment.updateMissionPath(missionProxy);
		}
	}

	@Override
	public void onMarkerDragEnd(MarkerInfo markerInfo) {
		checkForWaypointMarker(markerInfo);
	}

	private void checkForWaypointMarker(MarkerInfo markerInfo) {
		if ((markerInfo instanceof MissionItemMarkerInfo)) {
			missionProxy.move(((MissionItemMarkerInfo) markerInfo).getMarkerOrigin(),
					markerInfo.getPosition());
		}else if ((markerInfo instanceof PolygonMarkerInfo)) {
			PolygonMarkerInfo marker = (PolygonMarkerInfo) markerInfo;
			missionProxy.movePolygonPoint(marker.getSurvey(), marker.getIndex(), markerInfo.getPosition());
		}
	}

    @Override
    public void onApiConnected(){
        super.onApiConnected();
        zoomToFit();
    }

	@Override
	public void onMapClick(LatLong point) {
		editorListener.onMapClick(point);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        if(!(activity instanceof OnEditorInteraction)){
            throw new IllegalStateException("Parent activity must implement " +
                    OnEditorInteraction.class.getName());
        }

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
		final List<LatLong> visibleCoords = missionProxy == null ? new ArrayList<LatLong>() : missionProxy.getVisibleCoords();

		// add home coord if visible
		if(drone != null) {
			Home home = drone.getAttribute(AttributeType.HOME);
			if (home != null && home.isValid()) {
				final LatLong homeCoord = home.getCoordinate();
				if (homeCoord.getLongitude() != 0 && homeCoord.getLatitude() != 0)
					visibleCoords.add(homeCoord);
			}
		}

		if (!visibleCoords.isEmpty())
			zoomToFit(visibleCoords);
	}

    public void zoomToFit(List<LatLong> itemsToFit){
        if(!itemsToFit.isEmpty()){
            mMapFragment.zoomToFit(itemsToFit);
        }
    }

}
