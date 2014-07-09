package org.droidplanner.android.gcs;

import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.math.MathUtil;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class FollowMe implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener, OnDroneListener {
	private static final long MIN_TIME_MS = 500;
	private static final float MIN_DISTANCE_M = 0.0f;
	private Length radius = new Length(5.0);
	
	private Context context;
	private boolean followMeEnabled = false;
	private Drone drone;
	private LocationClient mLocationClient;
	
	private FollowModes currentFollowType = FollowModes.LEASH;
	
	/**
	 * °/s
	 */
	private static final double circleRate = 20;
	private double circleAngle = 0.0;

	public enum FollowModes {
		LEASH("Leash"), FIXED("Fixed"), HEADING("Heading"), WAKEBOARD(
				"Wakeboard"),CIRCLE("Circle");

		private String name;

		FollowModes(String str) {
			name = str;
		}
		@Override
		public String toString() {
			return name;
		}
	}

	public FollowMe(Context context, Drone drone) {
		this.context = context;
		this.drone = drone;
		mLocationClient = new LocationClient(context, this, this);
		mLocationClient.connect();
		drone.events.addDroneListener(this);
	}

	public void toggleFollowMeState() {
		if (isEnabled()) {
			disableFollowMe();
			drone.state.changeFlightMode(ApmModes.ROTOR_LOITER);
		} else {
			drone.state.changeFlightMode(ApmModes.ROTOR_GUIDED);
			enableFollowMe();
		}
	}

	private void enableFollowMe() {
		drone.events.notifyDroneEvent(DroneEventsType.FOLLOW_START);
		Log.d("follow", "enable");
		Toast.makeText(context, "FollowMe Enabled", Toast.LENGTH_SHORT).show();

		// Register the listener with the Location Manager to receive location
		// updates

		LocationRequest mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setInterval(MIN_TIME_MS);
		mLocationRequest.setFastestInterval(MIN_TIME_MS);
		mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_M);
		mLocationClient.requestLocationUpdates(mLocationRequest, this);

		followMeEnabled = true;
	}

	private void disableFollowMe() {
		if(followMeEnabled){
			Toast.makeText(context, "FollowMe Disabled", Toast.LENGTH_SHORT).show();
			followMeEnabled = false;
			Log.d("follow", "disable");
		}
		mLocationClient.removeLocationUpdates(this);
	}

	public boolean isEnabled() {
		return followMeEnabled;
	}


	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MODE:
			if ((drone.state.getMode() != ApmModes.ROTOR_GUIDED)) {
				disableFollowMe();
			}
			break;
		default:
			return;

		}

	}

	public Length getRadius() {
		return radius;
	}

	@Override
	public void onLocationChanged(Location location) {
		MavLinkROI.setROI(drone, new Coord3D(location.getLatitude(),location.getLongitude(), new Altitude(0.0)));
		
		switch (currentFollowType) {
		default:
		case LEASH:
			processNewLocationAsLeash(location);
			break;
		case HEADING:
			processNewLocationAsHeadingAngle(location);
			break;
		case WAKEBOARD:
			processNewLocationAsWakeboard(location);
			break;
		case CIRCLE:
			processNewLocationAsCircle(location);
			break;
		}
		
	}

	private void processNewLocationAsWakeboard(Location location) {
		Coord2D gcsCoord = new Coord2D(location.getLatitude(),
				location.getLongitude());
		float bearing = location.getBearing();
		
		Coord2D goToCoord;
		if (GeoTools.getDistance(gcsCoord, drone.GPS.getPosition())
				.valueInMeters() > radius.valueInMeters()) {
			double headingGCStoDrone = GeoTools.getHeadingFromCoordinates(
					gcsCoord, drone.GPS.getPosition());
			double userRigthHeading = 90.0 + bearing;
			double alpha = MathUtil.Normalize(location.getSpeed(),0.0,5.0);		
			double mixedHeading = MathUtil.bisectAngle(headingGCStoDrone,userRigthHeading,alpha);
			goToCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord,
					mixedHeading , radius.valueInMeters());
		}else{
			goToCoord = drone.guidedPoint.getCoord();
		}
		
		drone.guidedPoint.newGuidedCoord(goToCoord);		
	}

	private void processNewLocationAsHeadingAngle(Location location) {
		Coord2D gcsCoord = new Coord2D(location.getLatitude(),
				location.getLongitude());
		float bearing = location.getBearing();
		
		Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord,
				bearing+90.0, radius.valueInMeters());
		drone.guidedPoint.newGuidedCoord(goCoord);	
	}
	
	private void processNewLocationAsCircle(Location location) {
		Coord2D gcsCoord = new Coord2D(location.getLatitude(),
				location.getLongitude());
			Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord,
					circleAngle , radius.valueInMeters());
			circleAngle = MathUtil.constrainAngle(circleAngle + circleRate*MIN_TIME_MS/1000.0);
			drone.guidedPoint.newGuidedCoord(goCoord);
	}
	
	private void processNewLocationAsLeash(Location location) {
		Coord2D gcsCoord = new Coord2D(location.getLatitude(),
				location.getLongitude());
		if (GeoTools.getDistance(gcsCoord, drone.GPS.getPosition())
				.valueInMeters() > radius.valueInMeters()) {
			double headingGCStoDrone = GeoTools.getHeadingFromCoordinates(
					gcsCoord, drone.GPS.getPosition());
			Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord,
					headingGCStoDrone, radius.valueInMeters());
			drone.guidedPoint.newGuidedCoord(goCoord);
		}
	}

	public void changeRadius(Double increment) {
		radius = new Length(radius.valueInMeters()+ increment);
		if(radius.valueInMeters()<0)
			radius=new Length(0);
	}

	public void setType(FollowModes item) {
		currentFollowType = item;
	}
}