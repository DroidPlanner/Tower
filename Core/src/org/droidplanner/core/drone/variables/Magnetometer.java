package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;

import com.MAVLink.common.msg_raw_imu;

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
		return new int[] { x, y, z };
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public int[] getOffsets() {
		Parameter paramX = myDrone.getParameters().getParameter("COMPASS_OFS_X");
		Parameter paramY = myDrone.getParameters().getParameter("COMPASS_OFS_Y");
		Parameter paramZ = myDrone.getParameters().getParameter("COMPASS_OFS_Z");
		if (paramX == null || paramY == null || paramZ == null) {
			return null;
		}
		return new int[]{(int) paramX.value,(int) paramY.value,(int) paramZ.value};

	}
}
