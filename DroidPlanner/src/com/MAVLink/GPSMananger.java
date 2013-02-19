package com.MAVLink;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_global_position_int;
import com.diydrones.droidplanner.MAVLinkClient;
import com.diydrones.droidplanner.waypoint;

/**
 * Class to manage the comunication of GPS data from the MAV.
 * 
 * Should be initialized with a MAVLink Object, so the manager can send messages
 * via the MAV link. The function processMessage must be called with every new
 * MAV Message.
 * 
 */
public abstract class GPSMananger {
	/**
	 * Callback for when new GPS data is received
	 * 
	 * @param waypoints
	 *            list with received waypoints.
	 */
	public abstract void onGpsDataReceived(GPSdata data);

	/**
	 * Object with a MAVlink connection
	 */
	MAVLinkClient MAV;

	public class GPSdata{
		public waypoint position;
		public float heading;
	}
	
	public GPSMananger(MAVLinkClient MAV) {
		this.MAV = MAV;
	}

	/**
	 * Try to process a Mavlink message if it is a GPS related message
	 * 
	 * @param msg
	 *            Mavlink message to process
	 * @return Returns true if the message has been processed
	 */
	public boolean processMessage(MAVLinkMessage msg) {
		switch (msg.msgid) {
		case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
			processGpsIntMesssage((msg_global_position_int) msg);
			return true;
		default:
			return false;
		}
	}

	private void processGpsIntMesssage(msg_global_position_int msg) {
		GPSdata data = new GPSdata();
		data.position = new waypoint(msg.lat/1E7, msg.lon/1E7, msg.alt/1000.0);
		data.heading = (0x0000FFFF & ((int)msg.hdg))/100; // TODO fix unsigned short read at mavlink library
		onGpsDataReceived(data);
	}
}
