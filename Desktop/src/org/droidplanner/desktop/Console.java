package org.droidplanner.desktop;

import org.droidplanner.core.MAVLink.MAVLinkStreams.MAVLinkOutputStream;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.Preferences;
import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.drone.variables.Type.FirmwareType;

import com.MAVLink.Messages.MAVLinkPacket;

public class Console {

	public static Drone drone;

	public static void main(String[] args) {
		System.out.println("Hello");
		
		drone = droneFactory();
	}

	private static Drone droneFactory() {
		MAVLinkOutputStream MAVClient = new MAVLinkOutputStream() {
			
			@Override
			public void toggleConnectionState() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void sendMavPacket(MAVLinkPacket pack) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void queryConnectionState() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isConnected() {
				// TODO Auto-generated method stub
				return false;
			}
		};
		Clock clock = new Clock() {
			
			@Override
			public long elapsedRealtime() {
				// TODO Auto-generated method stub
				return 0;
			}
		};
		Preferences pref = new Preferences() {
			
			@Override
			public VehicleProfile loadVehicleProfile(FirmwareType firmwareType) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public FirmwareType getVehicleType() {
				// TODO Auto-generated method stub
				return FirmwareType.ARDU_COPTER;
			}
			
			@Override
			public Rates getRates() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		Handler handler = null;
		
		return new Drone(MAVClient, clock, handler, pref);
	}

}
