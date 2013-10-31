package com.droidplanner.drone.variables.mission.commands;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.droidplanner.R;
import com.droidplanner.fragments.markers.GenericMarker;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.mission.DialogMission;
import com.droidplanner.helpers.units.Altitude;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class RegionOfInterest extends MissionCMD implements MarkerSource{
	private Altitude altitude;
	private LatLng pointOfInterest;
	
	@Override
	public List<MarkerSource> getMarkers() throws Exception {
		ArrayList<MarkerSource> marker = new ArrayList<MarkerSource>();
		marker.add(this);
		return marker;
	}

	@Override
	public MarkerOptions build(Context context) {
		return GenericMarker.build(pointOfInterest).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_wp_loiter));
	}

	@Override
	public void update(Marker marker, Context context) {
		marker.setPosition(pointOfInterest);
	}
	
	@Override
	public DialogMission getDialog() {
		return null; // TODO create a dialog for ROI
	}

	public Altitude getHeight() {
		return altitude;
	}

	public void setHeight(Altitude value) {
		this.altitude = value;		
	}
	
	/*
	private static String getRoiDetail(GenericWaypoint wp, Context context) {
		if (wp.getParam1() == MAV_ROI.MAV_ROI_WPNEXT)
			return context.getString(R.string.next);
		else if (wp.getParam1() == MAV_ROI.MAV_ROI_TARGET)
			return String.format(Locale.US,"wp#%.0f", wp.getParam2());
		else if (wp.getParam1() == MAV_ROI.MAV_ROI_TARGET)
			return String.format(Locale.US,"tg#%.0f", wp.getParam2());
		else
			return "";
	}
	*/
}
