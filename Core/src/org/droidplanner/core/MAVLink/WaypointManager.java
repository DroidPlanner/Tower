package org.droidplanner.core.MAVLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.OnTimeout;
import org.droidplanner.core.drone.DroneInterfaces.OnWaypointManagerListener;
import org.droidplanner.core.drone.DroneVariable;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_count;
import com.MAVLink.Messages.ardupilotmega.msg_mission_current;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item_reached;
import com.MAVLink.Messages.ardupilotmega.msg_mission_request;

/**
 * Class to manage the communication of waypoints to the MAV.
 * 
 * Should be initialized with a MAVLink Object, so the manager can send messages
 * via the MAV link. The function processMessage must be called with every new
 * MAV Message.
 * 
 */
public class WaypointManager extends DroneVariable implements OnTimeout {
	enum WaypointStates {
		IDLE, READ_REQUEST, READING_WP, WRITING_WP_COUNT, WRITING_WP, WAITING_WRITE_ACK
	}

	public enum WaypointEvent_Type {
		WP_UPLOAD, WP_DOWNLOAD, WP_RETRY, WP_CONTINUE, WP_TIMED_OUT
	}

	private int readIndex;
	private int writeIndex;
	private int retryIndex;
	final private int maxRetry = 3;
	private TimeOut timeOut;
	private OnWaypointManagerListener wpEventListener;

	WaypointStates state = WaypointStates.IDLE;

	/**
	 * waypoint witch is currently being written
	 */

	public WaypointManager(Drone drone) {
		super(drone);
		this.timeOut = new TimeOut(this);
	}

	public WaypointManager(Drone myDrone, TimeOut timeOut) {
		super(myDrone);
		this.timeOut = timeOut;
	}

	public void setWaypointManagerListener(
			OnWaypointManagerListener wpEventListener) {
		this.wpEventListener = wpEventListener;
	}

	/**
	 * Try to receive all waypoints from the MAV.
	 * 
	 * If all runs well the callback will return the list of waypoints.
	 */
	public void getWaypoints() {
		// ensure that WPManager is not doing anything else
		if (state != WaypointStates.IDLE)
			return;

		doBeginWaypointEvent(WaypointEvent_Type.WP_DOWNLOAD);
		readIndex = -1;
		timeOut.setTimeOutValue(3000);
		timeOut.setTimeOutRetry(maxRetry);
		state = WaypointStates.READ_REQUEST;
		timeOut.setTimeOut();
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

	public void writeWaypoints(List<msg_mission_item> data) {
		// ensure that WPManager is not doing anything else
		if (state != WaypointStates.IDLE)
			return;

		if ((mission != null)) {
			doBeginWaypointEvent(WaypointEvent_Type.WP_UPLOAD);
			updateMsgIndexes(data);
			mission.clear();
			mission.addAll(data);
			writeIndex = 0;
			timeOut.setTimeOutValue(3000);
			timeOut.setTimeOutRetry(3);
			state = WaypointStates.WRITING_WP_COUNT;
			timeOut.setTimeOut();
			MavLinkWaypoint.sendWaypointCount(myDrone, mission.size());
		}
	}

	private void updateMsgIndexes(List<msg_mission_item> data) {
		short index = 0;
		for (msg_mission_item msg : data) {
			msg.seq = index++;
		}
	}

	/**
	 * Sets the current waypoint in the MAV
	 * 
	 * The callback will return the status of this operation
	 */
	public void setCurrentWaypoint(int i) {
		if ((mission != null)) {
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
	private List<msg_mission_item> mission = new ArrayList<msg_mission_item>();

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
				mission.clear();
				timeOut.setTimeOut();
				MavLinkWaypoint.requestWayPoint(myDrone, mission.size());
				state = WaypointStates.READING_WP;
				return true;
			}
			break;
		case READING_WP:
			if (msg.msgid == msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM) {
				timeOut.setTimeOut();
				processReceivedWaypoint((msg_mission_item) msg);
				doWaypointEvent(WaypointEvent_Type.WP_DOWNLOAD, readIndex + 1,
						waypointCount);
				if (mission.size() < waypointCount) {
					MavLinkWaypoint.requestWayPoint(myDrone, mission.size());
				} else {
					timeOut.resetTimeOut();
					state = WaypointStates.IDLE;
					MavLinkWaypoint.sendAck(myDrone);
					myDrone.mission.onMissionReceived(mission);
					doEndWaypointEvent(WaypointEvent_Type.WP_DOWNLOAD);
				}
				return true;
			}
			break;
		case WRITING_WP_COUNT:
			state = WaypointStates.WRITING_WP;
		case WRITING_WP:
			if (msg.msgid == msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST) {
				timeOut.setTimeOut();
				processWaypointToSend((msg_mission_request) msg);
				doWaypointEvent(WaypointEvent_Type.WP_UPLOAD, writeIndex + 1,
						mission.size());
				return true;
			}
			break;
		case WAITING_WRITE_ACK:
			if (msg.msgid == msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK) {
				timeOut.resetTimeOut();
				myDrone.mission.onWriteWaypoints((msg_mission_ack) msg);
				state = WaypointStates.IDLE;
				doEndWaypointEvent(WaypointEvent_Type.WP_UPLOAD);
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

	@Override
	public void notifyTimeOut(int timeOutCount) {
		processTimeOut(timeOutCount);
	}

	public boolean processTimeOut(int mTimeOutCount) {

		// If max retry is reached, set state to IDLE. No more retry.
		if (mTimeOutCount >= timeOut.getTimeOutRetry()) {
			state = WaypointStates.IDLE;
			doWaypointEvent(WaypointEvent_Type.WP_TIMED_OUT, retryIndex,
					maxRetry);
			return false;
		}

		retryIndex++;
		doWaypointEvent(WaypointEvent_Type.WP_RETRY, retryIndex, maxRetry);

		timeOut.setTimeOut(false);

		switch (state) {
		default:
		case IDLE:
			break;
		case READ_REQUEST:
			MavLinkWaypoint.requestWaypointsList(myDrone);
			break;
		case READING_WP:
			if (mission.size() < waypointCount) { // request last lost WP
				MavLinkWaypoint.requestWayPoint(myDrone, mission.size());
			}
			break;
		case WRITING_WP_COUNT:
			MavLinkWaypoint.sendWaypointCount(myDrone, mission.size());
			break;
		case WRITING_WP:
			// Log.d("TIMEOUT", "re Write Msg: " + String.valueOf(writeIndex));
			if (writeIndex < mission.size()) {
				myDrone.MavClient.sendMavPacket(mission.get(writeIndex).pack());
			}
			break;
		case WAITING_WRITE_ACK:
			myDrone.MavClient.sendMavPacket(mission.get(mission.size() - 1)
					.pack());
			break;
		}

		return true;
	}

	private void processWaypointToSend(msg_mission_request msg) {
		/*
		 * Log.d("TIMEOUT", "Write Msg: " + String.valueOf(msg.seq));
		 */
		writeIndex = msg.seq;
		myDrone.MavClient.sendMavPacket(mission.get(writeIndex).pack());

		if (writeIndex + 1 >= mission.size()) {
			state = WaypointStates.WAITING_WRITE_ACK;
		}
	}

	private void processReceivedWaypoint(msg_mission_item msg) {
		/*
		 * Log.d("TIMEOUT", "Read Last/Curr: " + String.valueOf(readIndex) + "/"
		 * + String.valueOf(msg.seq));
		 */
		// in case of we receive the same WP again after retry
		if (msg.seq <= readIndex)
			return;

		readIndex = msg.seq;

		mission.add(msg);
	}

	private void doBeginWaypointEvent(WaypointEvent_Type wpEvent) {
		retryIndex = 0;

		if (wpEventListener == null)
			return;

		wpEventListener.onBeginWaypointEvent(wpEvent);
	}

	private void doEndWaypointEvent(WaypointEvent_Type wpEvent) {
		if (retryIndex > 0)// if retry successful, notify that we now continue
			doWaypointEvent(WaypointEvent_Type.WP_CONTINUE, retryIndex,
					maxRetry);

		retryIndex = 0;

		if (wpEventListener == null)
			return;

		wpEventListener.onEndWaypointEvent(wpEvent);
	}

	private void doWaypointEvent(WaypointEvent_Type wpEvent, int index,
			int count) {
		retryIndex = 0;

		if (wpEventListener == null)
			return;

		wpEventListener.onWaypointEvent(wpEvent, index, count);
	}

	private class TimeOut {

		private Timer timeOutTimer;
		private int timeOutCount;
		private long timeOut;
		private int timeOutRetry;
		private OnTimeout listener;

		public TimeOut(OnTimeout listener) {
			this.listener = listener;
		}

		public void setTimeOutValue(long timeout_ms) {
			this.timeOut = timeout_ms;
		}

		public void setTimeOutRetry(int timeout_retry) {
			this.timeOutRetry = timeout_retry;
		}

		public int getTimeOutRetry() {
			if (this.timeOutRetry <= 0)
				return 3; // default value

			return this.timeOutRetry;
		}

		public synchronized void resetTimeOut() {
			if (timeOutTimer != null) {
				timeOutTimer.cancel();
				timeOutTimer = null;
				/*
				 * Log.d("TIMEOUT", "reset " + String.valueOf(timeOutTimer));
				 */
			}
		}

		public void setTimeOut() {
			setTimeOut(this.timeOut, true);
		}

		public void setTimeOut(boolean resetTimeOutCount) {
			setTimeOut(this.timeOut, resetTimeOutCount);
		}

		public synchronized void setTimeOut(long timeout_ms,
				boolean resetTimeOutCount) {
			/*
			 * Log.d("TIMEOUT", "set " + String.valueOf(timeout_ms));
			 */
			resetTimeOut();
			if (resetTimeOutCount)
				timeOutCount = 0;

			if (timeOutTimer == null) {
				timeOutTimer = new Timer();
				timeOutTimer.schedule(new TimerTask() {
					public void run() {
						if (timeOutTimer != null) {
							resetTimeOut();
							timeOutCount++;

							/*
							 * Log.d("TIMEOUT", "timed out");
							 */

							listener.notifyTimeOut(timeOutCount);
						}
					}
				}, timeout_ms); // delay in milliseconds
			}
		}

	}

}
