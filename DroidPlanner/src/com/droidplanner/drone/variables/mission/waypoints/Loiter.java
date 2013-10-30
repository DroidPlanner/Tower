package com.droidplanner.drone.variables.mission.waypoints;

import android.content.Context;

import com.droidplanner.R;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.markers.helpers.MarkerWithText;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

public abstract class Loiter extends GenericWaypoint implements MarkerSource {

	private double radius;
	private double angle;
	
	public Loiter(LatLng coord, double altitude) {
		super(coord, altitude);
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}
	
	public double getAngle() {
		return this.angle;
	}
	
	public double getRadius(){
		return this.radius;
	}
	@Override
	protected BitmapDescriptor getIcon(Context context) {
		return BitmapDescriptorFactory.fromBitmap(MarkerWithText
				.getMarkerWithTextAndDetail(R.drawable.ic_wp_loiter, "text",
						"detail", context));
	}
	
	
}