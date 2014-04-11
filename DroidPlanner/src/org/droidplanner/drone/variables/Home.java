package org.droidplanner.drone.variables;

import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneVariable;
import org.droidplanner.fragments.markers.HomeMarker;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.helpers.geoTools.GeoTools;
import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.helpers.units.Length;

import android.content.Context;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_FRAME;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Home extends DroneVariable implements MarkerSource {
	private LatLng coordinate;
	private Altitude altitude = new Altitude(0);
	
	public Home(Drone drone) {
		super(drone);
	}
	
	@Override
	public MarkerOptions build(Context context) {
		return HomeMarker.build(this);
	}

	@Override
	public void update(Marker marker, Context context) {
		HomeMarker.update(marker, this);
	}


	public boolean isValid() {
		return (coordinate!=null);
	}
	
	public Home getHome() {
		return this;
	}
		
	public Length getDroneDistanceToHome() {
		if (isValid()) {
			return GeoTools.getDistance(coordinate, myDrone.GPS.getPosition());			
		}else{
			return new Length(0); // TODO fix this
		}
	}

	public LatLng getCoord() {
		return coordinate;
	}

    public boolean hasCoordinates() {
        return coordinate != null && coordinate.latitude != 0 && coordinate.longitude != 0;
    }

	public Length getAltitude() {
		return altitude;
	}

	public void setHome(msg_mission_item msg) {
		this.coordinate = new LatLng(msg.x, msg.y);
		this.altitude = new Altitude(msg.z);		
		myDrone.events.notifyDroneEvent(DroneEventsType.HOME);
	}

	public msg_mission_item packMavlink() {
		msg_mission_item mavMsg = new msg_mission_item();
		mavMsg.autocontinue = 1;
		mavMsg.current = 1;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
		mavMsg.target_component = 1;
		mavMsg.target_system = 1;
		if (isValid()) {
			mavMsg.x = (float) getCoord().latitude;
			mavMsg.y = (float) getCoord().longitude;
			mavMsg.z = (float) getAltitude().valueInMeters();			
		}
		return mavMsg;
	}

}
