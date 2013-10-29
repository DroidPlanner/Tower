package com.droidplanner.drone.variables;

import android.content.Context;
import android.widget.Toast;

import com.droidplanner.MAVLink.MavLinkModes;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;
import com.droidplanner.fragments.markers.GuidedMarker;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GuidedPoint extends DroneVariable implements MarkerSource {
	private OnGuidedListener listner;
	private LatLng coord;

	public interface OnGuidedListener {
		public void onGuidedPoint(GuidedPoint guidedPoint);
	}

	public GuidedPoint(Drone myDrone) {
		super(myDrone);
	}

	@Override
	public MarkerOptions build(Context context) {
		return GuidedMarker.build(this, myDrone.mission.getDefaultAlt(), context);
	}

	@Override
	public void update(Marker markerFromGcp, Context context) {
		GuidedMarker.update(markerFromGcp, this, myDrone.mission.getDefaultAlt(), context);
	}

	public LatLng getCoord() {
		return coord;
	}

	public void setCoord(LatLng coord)
	{
		this.coord = coord;
	}

	public void newGuidedPoint(LatLng coord) {
		this.coord = coord;
		listner.onGuidedPoint(this);
	}

	public void setOnGuidedListner(OnGuidedListener listner) {
		this.listner = listner;
	}

	public void setGuidedMode() {
		Double altitude = myDrone.mission.getDefaultAlt();
		MavLinkModes.setGuidedMode(myDrone, coord.latitude,coord.longitude,myDrone.mission.getDefaultAlt());
		Toast.makeText(myDrone.context, "Guided Mode (" + altitude + "m)",
				Toast.LENGTH_SHORT).show();
	}	

	public void invalidateCoord() {
		coord = null;
	}

	public boolean isCoordValid() {
		return coord != null;
	}

}