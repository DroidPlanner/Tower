package com.droidplanner.drone.variables;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_count;
import com.MAVLink.Messages.ardupilotmega.msg_mission_current;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item_reached;
import com.MAVLink.Messages.ardupilotmega.msg_mission_request;
import com.droidplanner.MAVLink.MavLinkWaypoint;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

/**
 * Class to manage the communication of waypoints to the MAV.
 * 
 * Should be initialized with a MAVLink Object, so the manager can send messages
 * via the MAV link. The function processMessage must be called with every new
 * MAV Message.
 * 
 */
public class WaypointMananger extends DroneVariable {
	enum waypointStates {
		IDLE, READ_REQUEST, READING_WP, WRITTING_WP_COUNT, WRITTING_WP, WAITING_WRITE_ACK
	}

	private int lastWPSeq;
	private int writeIndex;

	waypointStates state = waypointStates.IDLE;

	/**
	 * Try to receive all waypoints from the MAV.
	 * 
	 * If all runs well the callback will return the list of waypoints.
	 */
	public void getWaypoints() {
		// ensure that WPManager is not doing anything else		
		if (state != waypointStates.IDLE)
			return;

		lastWPSeq = -1;
		myDrone.MavClient.setTimeOutValue(3000);
		myDrone.MavClient.setTimeOutRetry(3);
		state = waypointStates.READ_REQUEST;
		myDrone.MavClient.setTimeOut();
		MavLinkWaypoint.requestWaypointsList(myDrone);
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
		// ensure that WPManager is not doing anything else		
		if (state != waypointStates.IDLE)
			return;

		if ((waypoints != null)) {
			waypoints.clear();
			waypoints.addAll(data);
			writeIndex = 0;
			lastWPSeq = -1;
			myDrone.MavClient.setTimeOutValue(3000);
			myDrone.MavClient.setTimeOutRetry(3);
			state = waypointStates.WRITTING_WP_COUNT;
			myDrone.MavClient.setTimeOut();
			MavLinkWaypoint.sendWaypointCount(myDrone, waypoints.size());
		}
	}

	/**
	 * Sets the current waypoint in the MAV
	 * 
	 * The callback will return the status of this operation
	 */
	public void setCurrentWaypoint(int i) {
		if ((waypoints != null)) {
			MavLinkWaypoint.sendSetCurrentWaypoint(myDrone, (short) i);
		}
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
	 * number of waypoints to be received, used when reading waypoints
	 */
	private short waypointCount;
	/**
	 * list of waypoints used when writing or receiving
	 */
	private List<waypoint> waypoints = new ArrayList<waypoint>();

	/**
	 * waypoint witch is currently being written
	 */

	public WaypointMananger(Drone drone) {
		super(drone);
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
				waypointCount = ((msg_mission_count) msg).count;
				waypoints.clear();
				myDrone.MavClient.setTimeOut();
				MavLinkWaypoint.requestWayPoint(myDrone, waypoints.size());
				state = waypointStates.READING_WP;
				return true;
			}
			break;
		case READING_WP:
			if (msg.msgid == msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM) {
				myDrone.MavClient.setTimeOut();
				processReceivedWaypoint((msg_mission_item) msg);
				if (waypoints.size() < waypointCount) {
					MavLinkWaypoint.requestWayPoint(myDrone, waypoints.size());
				} else {
					myDrone.MavClient.resetTimeOut();
					state = waypointStates.IDLE;
					MavLinkWaypoint.sendAck(myDrone);
					myDrone.mission.onWaypointsReceived(waypoints);
				}
				return true;
			}
			break;
		case WRITTING_WP_COUNT:
			state = waypointStates.WRITTING_WP;
		case WRITTING_WP:
			if (msg.msgid == msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST) {
				myDrone.MavClient.setTimeOut();
				processWaypointToSend((msg_mission_request) msg);
				return true;
			}
			break;
		case WAITING_WRITE_ACK:
			if (msg.msgid == msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK) {
				myDrone.MavClient.resetTimeOut();
				myDrone.mission.onWriteWaypoints((msg_mission_ack) msg);
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

	public boolean processTimeOut(int mTimeOutCount) {
		
		//If max retry is reached, set state to IDLE. No more retry.
		if (mTimeOutCount >= myDrone.MavClient.getTimeOutRetry()){
			state = waypointStates.IDLE;
			return false;
		}

		myDrone.MavClient.setTimeOut(false);

		switch (state) {
		default:
		case IDLE:
			break;
		case READ_REQUEST:
			MavLinkWaypoint.requestWaypointsList(myDrone);
			break;
		case READING_WP:
			if (waypoints.size() < waypointCount) { // request last lost WP
				MavLinkWaypoint.requestWayPoint(myDrone, waypoints.size());
			}
			break;
		case WRITTING_WP_COUNT:
			MavLinkWaypoint.sendWaypointCount(myDrone, waypoints.size());
			break;
		case WRITTING_WP:
			Log.d("TIMEOUT",
					"re Write Msg: " + String.valueOf(writeIndex));
			if(writeIndex<waypoints.size()){
				MavLinkWaypoint.sendWaypoint(myDrone, writeIndex,
						waypoints.get(writeIndex));				
			}
			break;
		case WAITING_WRITE_ACK:
			MavLinkWaypoint.sendWaypoint(myDrone, waypoints.size()-1,
					waypoints.get(waypoints.size()-1));
			break;
		}

		return true;
	}

	private void processWaypointToSend(msg_mission_request msg) {
		Log.d("TIMEOUT",
				"Write Msg: " + String.valueOf(msg.seq));
		writeIndex = msg.seq;
		MavLinkWaypoint.sendWaypoint(myDrone, writeIndex,
				waypoints.get(writeIndex));
		
		if (writeIndex+1 >= waypoints.size()) {
			state = waypointStates.WAITING_WRITE_ACK;
		}
	}

	private void processReceivedWaypoint(msg_mission_item msg) {
		Log.d("TIMEOUT",
				"Read Last/Curr: " + String.valueOf(lastWPSeq) + "/"
						+ String.valueOf(msg.seq));
		
		// in case of we receive the same WP again after retry
		if (msg.seq <= lastWPSeq)
			return;

		lastWPSeq = msg.seq;
		waypoints.add(new waypoint(msg));
	}
}
