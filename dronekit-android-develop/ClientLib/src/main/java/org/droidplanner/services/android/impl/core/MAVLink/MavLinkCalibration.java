package org.droidplanner.services.android.impl.core.MAVLink;

import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_CMD_ACK;

import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import com.o3dr.services.android.lib.model.ICommandListener;

public class MavLinkCalibration {

	public static void sendCalibrationAckMessage(MavLinkDrone drone, int count) {
		msg_command_ack msg = new msg_command_ack();
		msg.command = count;
		msg.result = MAV_CMD_ACK.MAV_CMD_ACK_OK;
		drone.getMavClient().sendMessage(msg, null);
	}

	public static void startAccelerometerCalibration(MavLinkDrone drone, ICommandListener listener) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();

		msg.command = MAV_CMD.MAV_CMD_PREFLIGHT_CALIBRATION;
		msg.param1 = 0;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 1;
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;
		drone.getMavClient().sendMessage(msg, listener);
	}

	/**
	 * Initiate a magnetometer calibration
	 * @param drone
	 */
	public static void startMagnetometerCalibration(MavLinkDrone drone, ICommandListener listener){
		startMagnetometerCalibration(drone, false, false, 0, listener);
	}

	/**
	 * Initiate a magnetometer calibration
	 * @param drone vehicle to calibrate
	 * @param retryOnFailure if true, automatically retry the magnetometer calibration if it fails
	 * @param saveAutomatically if true, save the calibration automatically without user input.
	 * @param startDelay positive delay in seconds before starting the calibration
	 */
	public static void startMagnetometerCalibration(MavLinkDrone drone, boolean retryOnFailure, boolean saveAutomatically,
													int startDelay, ICommandListener listener){
		msg_command_long msg = new msg_command_long();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();

		msg.command = MAV_CMD.MAV_CMD_DO_START_MAG_CAL;
		msg.param1 = 0;
		msg.param2 = retryOnFailure ? 1 : 0;
		msg.param3 = saveAutomatically ? 1 : 0;
		msg.param4 = startDelay > 0 ? startDelay : 0;
		msg.param5 = 0;
		msg.param6 = 0;
		msg.param7 = 0;

		drone.getMavClient().sendMessage(msg, listener);
	}

	/**
	 * Cancel the running magnetometer calibration.˛
	 * @param drone
	 */
	public static void cancelMagnetometerCalibration(MavLinkDrone drone, ICommandListener listener){
		msg_command_long msg = new msg_command_long();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();

		msg.command = MAV_CMD.MAV_CMD_DO_CANCEL_MAG_CAL;
		msg.param1 = 0;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 0;
		msg.param6 = 0;
		msg.param7 = 0;

		drone.getMavClient().sendMessage(msg, listener);
	}

	/**
	 * Accept the magnetometer calibration result.
	 * @param drone
	 */
	public static void acceptMagnetometerCalibration(MavLinkDrone drone, ICommandListener listener){
		msg_command_long msg = new msg_command_long();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();

		msg.command = MAV_CMD.MAV_CMD_DO_ACCEPT_MAG_CAL;
		msg.param1 = 0;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 0;
		msg.param6 = 0;
		msg.param7 = 0;

		drone.getMavClient().sendMessage(msg, listener);
	}

}
