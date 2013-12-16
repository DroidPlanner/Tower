package com.droidplanner.drone.variables;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_global_position_int;
import com.MAVLink.Messages.ardupilotmega.msg_gps_raw_int;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;
import com.google.android.gms.maps.model.LatLng;

public class GPS extends DroneVariable {
	private double gps_eph = -1;
	private int satCount = -1;
	private int fixType = -1;
	private LatLng position;

	public GPS(Drone myDrone) {
		super(myDrone);
	}

	public boolean isPositionValid() {
		return (position != null);
	}

	public LatLng getPosition() {
		if (isPositionValid()) {
			return position;
		} else {
			return new LatLng(0, 0);
		}
	}

	public double getGpsEPH() {
		return gps_eph;
	}

	public int getSatCount() {
		return satCount;
	}

	public String getFixType() {
		String gpsFix = "";
		switch (fixType) {
		case 2:
			gpsFix = ("2D");
			break;
		case 3:
			gpsFix = ("3D");
			break;
		default:
			gpsFix = ("NoFix");
			break;
		}
		return gpsFix;
	}

	public void setGpsState(int fix, int satellites_visible, int eph) {
		if (satCount != satellites_visible | fixType != fix) {
			if (fixType != fix) {
				myDrone.tts.speakGpsMode(fix);
			}
			fixType = fix;
			satCount = satellites_visible;
			gps_eph = (double) eph / 100; // convert from eph(cm) to gps_eph(m)

			myDrone.notifyInfoChange();
		}
	}

	public void setPosition(LatLng position) {
		if (this.position != position) {
			this.position = position;
			myDrone.notifyPositionChange();
			myDrone.notifyDistanceToHomeChange();
		}
	}

	@Override
	protected void processMAVLinkMessage(MAVLinkMessage msg) {
		switch (msg.msgid) {
		case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
			setGpsState(((msg_gps_raw_int) msg).fix_type,
					((msg_gps_raw_int) msg).satellites_visible,
					((msg_gps_raw_int) msg).eph);
			break;
		case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
			setPosition(new LatLng(((msg_global_position_int) msg).lat / 1E7,
					((msg_global_position_int) msg).lon / 1E7));
			break;
		}

	}
}