package com.droidplanner.drone.variables.mission.waypoints;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.droidplanner.drone.variables.mission.Mission;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.fragments.markers.GenericMarker;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.markers.helpers.MarkerWithText;
import com.droidplanner.helpers.units.Altitude;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Generic Mission item with Spatial Coordinates
 *
 */
public abstract class SpatialCoordItem extends MissionItem implements
		MarkerSource {

	protected abstract int getIconDrawable();
	protected abstract int getIconDrawableSelected();

	LatLng coordinate;
	Altitude altitude;

	public SpatialCoordItem(Mission mission, LatLng coord, Altitude altitude) {
		super(mission);
		this.coordinate = coord;
		this.altitude = altitude;
	}

	public SpatialCoordItem(MissionItem item) {
		super(item);
		if (item instanceof SpatialCoordItem) {
			coordinate = ((SpatialCoordItem) item).getCoordinate();
			altitude = ((SpatialCoordItem) item).getAltitude();
		} else {
			coordinate = new LatLng(0, 0);
			altitude = new Altitude(0);
		}
	}

	@Override
	public MarkerOptions build(Context context) {
		return GenericMarker.build(coordinate).icon(getIcon(context));
	}

	@Override
	public void update(Marker marker, Context context) {
		marker.setPosition(coordinate);
		marker.setIcon(getIcon(context));
	}

	@Override
	public List<MarkerSource> getMarkers() throws Exception {
		ArrayList<MarkerSource> marker = new ArrayList<MarkerSource>();
		marker.add(this);
		return marker;
	}

	@Override
	public List<LatLng> getPath() throws Exception {
		ArrayList<LatLng> points = new ArrayList<LatLng>();
		points.add(coordinate);
		return points;
	}

	protected BitmapDescriptor getIcon(Context context) {
		int drawable;
		if (selected) {
			drawable = getIconDrawableSelected();
		} else {
			drawable = getIconDrawable();
		}
		Bitmap marker = MarkerWithText.getMarkerWithTextAndDetail(drawable, getIconText(),
						getIconDetail(), context);
		return BitmapDescriptorFactory.fromBitmap(marker);
	}

	private String getIconDetail() {
		if (mission.checkIfAltitudeHasChangedFromPreviusItem(this)) {
			return null; //altitude.toString();
		}else{
			return null;
		}
	}

	private String getIconText() {
		return Integer.toString(mission.getNumber(this));
	}

	public void setCoordinate(LatLng position) {
		coordinate = position;
	}

	public LatLng getCoordinate() {
		return coordinate;
	}

	public Altitude getAltitude() {
		return altitude;
	}

	public void setAltitude(Altitude altitude) {
		this.altitude = altitude;
	}

	@Override
	public msg_mission_item packMissionItem() {
		msg_mission_item mavMsg = new msg_mission_item();
		mavMsg.autocontinue = 1;
		mavMsg.target_component = 1;
		mavMsg.target_system = 1;
		mavMsg.x = (float) getCoordinate().latitude;
		mavMsg.y = (float) getCoordinate().longitude;
		mavMsg.z = (float) getAltitude().valueInMeters();
//		mavMsg.compid =
		return mavMsg;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		LatLng coord = new LatLng(mavMsg.x,mavMsg.y);
		Altitude alt = new Altitude(mavMsg.z);
		setCoordinate(coord);
		setAltitude(alt);
	}
}