package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.model.Drone;

import com.MAVLink.Messages.ardupilotmega.msg_raw_imu;

public class Magnetometer extends DroneVariable {

	private int x;
	private int y;
	private int z;

	public Magnetometer(Drone myDrone) {
		super(myDrone);
	}

	public void newData(msg_raw_imu msg_imu) {
		x = msg_imu.xmag;
		y = msg_imu.ymag;
		z = msg_imu.zmag;		
		myDrone.notifyDroneEvent(DroneEventsType.MAGNETOMETER);
	}

	public int[] getVector() {
		return new int[] {x,y,z};
	}

}
