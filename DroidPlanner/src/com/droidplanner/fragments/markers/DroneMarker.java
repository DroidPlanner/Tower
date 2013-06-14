package com.droidplanner.fragments.markers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.MAVLink.Messages.enums.MAV_TYPE;
import com.droidplanner.R.drawable;
import com.droidplanner.MAVLink.Drone.MapUpdatedListner;
import com.droidplanner.fragments.FlightMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DroneMarker implements MapUpdatedListner {
	public static final int DRONE_MIN_ROTATION = 5;
	
	public BitmapDescriptor[] droneBitmaps;
	public Marker droneMarker;
	private FlightMapFragment flightMapFragment;

	public DroneMarker(FlightMapFragment flightMapFragment) {
		this.flightMapFragment = flightMapFragment;
	}

	public void buildBitmaps(FlightMapFragment flightMapFragment, int type) {
		int count = 360/DRONE_MIN_ROTATION;
		droneBitmaps = new BitmapDescriptor[count];
		for (int i = 0; i < count; i++) {					
			droneBitmaps[i] = flightMapFragment.droneMarker.generateIcon(i*DRONE_MIN_ROTATION,type); 
		}
		
	}

	public BitmapDescriptor generateIcon(float heading, int type) {
		Bitmap planeBitmap = flightMapFragment.droneMarker.getBitmap(type);
		Matrix matrix = new Matrix();
		matrix.postRotate(heading - flightMapFragment.mMap.getCameraPosition().bearing);
		return BitmapDescriptorFactory.fromBitmap( Bitmap.createBitmap(planeBitmap, 0, 0, planeBitmap.getWidth(),
				planeBitmap.getHeight(), matrix, true));
	}

	public Bitmap getBitmap(int type) {
		switch (type) {
		case MAV_TYPE.MAV_TYPE_TRICOPTER:
		case MAV_TYPE.MAV_TYPE_QUADROTOR:
		case MAV_TYPE.MAV_TYPE_HEXAROTOR:
		case MAV_TYPE.MAV_TYPE_OCTOROTOR:
		case MAV_TYPE.MAV_TYPE_HELICOPTER:
			return BitmapFactory
					.decodeResource(flightMapFragment.getResources(), drawable.quad);
		case MAV_TYPE.MAV_TYPE_FIXED_WING:
		default:
			return BitmapFactory.decodeResource(flightMapFragment.getResources(),
					drawable.plane);
		}
	}

	public void updatePosition(double yaw, LatLng coord) {
		double correctHeading = (yaw - flightMapFragment.getMapRotation()+360)%360;	// This ensure the 0 to 360 range
		int index = (int) (correctHeading/DRONE_MIN_ROTATION);
		
		try{
			droneMarker.setPosition(coord);
			droneMarker.setIcon(droneBitmaps[index]);
			
			if(!flightMapFragment.hasBeenZoomed){
				flightMapFragment.hasBeenZoomed = true;
				flightMapFragment.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 16));
			}
			
			if(flightMapFragment.isAutoPanEnabled){
				flightMapFragment.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(droneMarker.getPosition(), 17));
			}
		}catch(Exception e){
		}
	}

	public void updateDroneMarkers(){
		buildBitmaps(flightMapFragment, flightMapFragment.drone.getType());
		droneMarker = flightMapFragment.mMap.addMarker(new MarkerOptions()
		.anchor((float) 0.5, (float) 0.5)
		.position(new LatLng(0, 0))
		.icon(droneBitmaps[0])
		.visible(false));			
	}

	public void onDroneUpdate() {
		updatePosition(flightMapFragment.drone.getYaw(), flightMapFragment.drone.getPosition());
		flightMapFragment.addFlithPathPoint(flightMapFragment.drone.getPosition());		
	}
}