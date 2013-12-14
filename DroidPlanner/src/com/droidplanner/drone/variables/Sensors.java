package com.droidplanner.drone.variables;

import com.MAVLink.Messages.ardupilotmega.msg_raw_imu;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class Sensors extends DroneVariable{
	
	public double xacc;
	public double yacc;
	public double zacc;

	public Sensors(Drone myDrone) {
		super(myDrone);
	}

	public void newImuData(msg_raw_imu msg) {
		xacc = msg.xacc/1000f;
		yacc = msg.yacc/1000f;
		zacc = msg.zacc/1000f;
		if(myDrone.sensorDataListner!=null){
			myDrone.sensorDataListner.onNewAccelData();
		}
	}
	
	

}
