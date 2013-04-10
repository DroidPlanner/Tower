package com.MAVLink;

import java.util.ArrayList;
import java.util.List;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_count;
import com.MAVLink.Messages.ardupilotmega.msg_mission_current;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item_reached;
import com.MAVLink.Messages.ardupilotmega.msg_mission_request;
import com.MAVLink.Messages.ardupilotmega.msg_mission_request_list;
import com.MAVLink.Messages.ardupilotmega.msg_mission_set_current;
import com.droidplanner.service.MAVLinkClient;

/**
 * Class to manage the communication of waypoints to the MAV.
 * 
 * Should be initialized with a MAVLink Object, so the manager can send messages
 * via the MAV link. The function processMessage must be called with every new
 * MAV Message.
 * 
 */
public class WaypointMananger {
	/**
	 * Try to receive all waypoints from the MAV.
	 * 
	 * If all runs well the callback will return the list of waypoints.
	 */
	public void getWaypoints() {
			state = waypointStates.READ_REQUEST;
			requestWaypointsList();
	}

	/**
	 * Write a list of waypoints to the MAV.
	 * 
	 * The callback will return the status of this operation
	 * 
	 * @param data
	 *            waypoints to be written
	 */
	public void writeWaypoints(List<waypoint> data) {
		if ((waypoints != null)) {
			waypoints.clear();
			waypoints.addAll(data);
			writeIndex = 0;
			state =waypointStates.WRITTING_WP;
			sendWaypointCount();
		}
	}

	/**
	 * Sets the current waypoint in the MAV
	 * 
	 * The callback will return the status of this operation
	 * 
	 * @param data
	 *            waypoints to be written
	 */
	public void setCurrentWaypoint(int i) {
		if ((waypoints != null)) {
			sendSetCurrentWaypoint((short )i);
		}
	}

	public interface OnWaypointManagerListner{
		public abstract void onWaypointsReceived(List<waypoint> waypoints);
		public abstract void onWriteWaypoints(msg_mission_ack msg);
	}

	/**
	 * Callback for when a waypoint has been reached
	 * 
	 * @param wpNumber
	 *            number of the completed waypoint
	 */
	public void onWaypointReached(int wpNumber) {
	}

	/**
	 * Callback for a change in the current waypoint the MAV is heading for
	 * 
	 * @param seq
	 *            number of the updated waypoint
	 */
	private void onCurrentWaypointUpdate(short seq) {
	}

	/**
	 * Object with a MAVlink connection
	 */
	MAVLinkClient MAV;
	private OnWaypointManagerListner listner;
	/**
	 * number of waypoints to be received, used when reading waypoints
	 */
	private short waypointCount;
	/**
	 * list of waypoints used when writing or receiving
	 */
	private List<waypoint> waypoints;
	/**
	 * waypoint witch is currently being written
	 */
	private int writeIndex;

	enum waypointStates {
		IDLE, READ_REQUEST, READING_WP, WRITTING_WP, WAITING_WRITE_ACK
	}

	waypointStates state = waypointStates.IDLE;
	public WaypointMananger(MAVLinkClient MAV, OnWaypointManagerListner listner) {
		this.MAV = MAV;
		this.listner = listner;
		waypoints = new ArrayList<waypoint>();
	}

	/**
	 * Try to process a Mavlink message if it is a mission related message
	 * 
	 * @param msg
	 *            Mavlink message to process
	 * @return Returns true if the message has been processed
	 */
	public boolean processMessage(MAVLinkMessage msg) {
		switch (state) {
		default:
		case IDLE:
			break;
		case READ_REQUEST:
			if (msg.msgid == msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT) {
				requestFirstWaypoint(msg);
				state = waypointStates.READING_WP;
				return true;
			}
			break;
		case READING_WP:
			if (msg.msgid == msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM) {
				processReceivedWaypoint((msg_mission_item) msg);
				if (waypoints.size() < waypointCount) {
					requestWayPoint();
				} else {
					state = waypointStates.IDLE;
					sendAck();
					listner.onWaypointsReceived(waypoints);
				}
				return true;
			}
			break;
		case WRITTING_WP:
			if (msg.msgid == msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST) {
				sendWaypoint(writeIndex++);
				if (writeIndex >= waypoints.size()) {
					state = waypointStates.WAITING_WRITE_ACK;
				}
				return true;
			}
			break;
		case WAITING_WRITE_ACK:
			if (msg.msgid == msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK) {
				listner.onWriteWaypoints((msg_mission_ack) msg);
				state = waypointStates.IDLE;
				return true;
			}
			break;
		}

		if (msg.msgid == msg_mission_item_reached.MAVLINK_MSG_ID_MISSION_ITEM_REACHED) {
			onWaypointReached(((msg_mission_item_reached) msg).seq);
			return true;
		}
		if (msg.msgid == msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT) {
			onCurrentWaypointUpdate(((msg_mission_current) msg).seq);
			return true;
		}
		return false;
	}

	private void requestFirstWaypoint(MAVLinkMessage msg) {
		waypointCount = ((msg_mission_count) msg).count;
		waypoints.clear();
		requestWayPoint();
	}

	private void processReceivedWaypoint(msg_mission_item msg) {
		Double Lat = (double) msg.x;
		Double Lng = (double) msg.y;
		Double h = (double) msg.z;
		waypoints.add(new waypoint(Lat, Lng, h));
	}

	private void sendAck() {
		msg_mission_ack msg = new msg_mission_ack();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.type = 0; // TODO use MAV_MISSION_RESULT constant
		MAV.sendMavPacket(msg.pack());

	}

	private void requestWaypointsList() {
		msg_mission_request_list msg = new msg_mission_request_list();
		msg.target_system = 1;
		msg.target_component = 1;
		MAV.sendMavPacket(msg.pack());
	}

	private void requestWayPoint() {
		msg_mission_request msg = new msg_mission_request();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.seq = (short) waypoints.size();
		MAV.sendMavPacket(msg.pack());
	}

	private void sendWaypointCount() {
		msg_mission_count msg = new msg_mission_count();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.count = (short) waypoints.size();
		MAV.sendMavPacket(msg.pack());
	}

	private void sendWaypoint(int index) {
		msg_mission_item msg = new msg_mission_item();
		msg.seq = (short) index;
		msg.current = (byte) ((index == 0) ? 1 : 0); // TODO use correct
														// parameter for HOME
		msg.frame = 0; // TODO use correct parameter
		msg.command = 16; // TODO use correct parameter
		msg.param1 = 0; // TODO use correct parameter
		msg.param2 = 0; // TODO use correct parameter
		msg.param3 = 0; // TODO use correct parameter
		msg.param4 = 0; // TODO use correct parameter
		msg.x = (float) waypoints.get(index).coord.latitude;
		msg.y = (float) waypoints.get(index).coord.longitude;
		msg.z = waypoints.get(index).Height.floatValue();
		msg.autocontinue = 1; // TODO use correct parameter
		msg.target_system = 1;
		msg.target_component = 1;
		MAV.sendMavPacket(msg.pack());
	}

	private void sendSetCurrentWaypoint(short i) {
		msg_mission_set_current msg = new msg_mission_set_current();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.seq = i;
		MAV.sendMavPacket(msg.pack());
	}
}
