package com.MAVLink.Messages;

import android.util.Log;
import java.io.Serializable;
import com.MAVLink.Messages.ardupilotmega.*;

/**
 * Common interface for all MAVLink Messages
 * Packet Anatomy
 * This is the anatomy of one packet. It is inspired by the CAN and SAE AS-4 standards. 

 * Byte Index  Content              Value       Explanation  
 * 0            Packet start sign  v1.0: 0xFE   Indicates the start of a new packet.  (v0.9: 0x55) 
 * 1            Payload length      0 - 255     Indicates length of the following payload.  
 * 2            Packet sequence     0 - 255     Each component counts up his send sequence. Allows to detect packet loss  
 * 3            System ID           1 - 255     ID of the SENDING system. Allows to differentiate different MAVs on the same network.  
 * 4            Component ID        0 - 255     ID of the SENDING component. Allows to differentiate different components of the same system, e.g. the IMU and the autopilot.  
 * 5            Message ID          0 - 255     ID of the message - the id defines what the payload means and how it should be correctly decoded.  
 * 6 to (n+6)   Payload             0 - 255     Data of the message, depends on the message id.  
 * (n+7)to(n+8) Checksum (low byte, high byte)  ITU X.25/SAE AS-4 hash, excluding packet start sign, so bytes 1..(n+6) Note: The checksum also includes MAVLINK_CRC_EXTRA (Number computed from message fields. Protects the packet from decoding a different version of the same packet but with different variables).  

 * The checksum is the same as used in ITU X.25 and SAE AS-4 standards (CRC-16-CCITT), documented in SAE AS5669A. Please see the MAVLink source code for a documented C-implementation of it. LINK TO CHECKSUM
 * The minimum packet length is 8 bytes for acknowledgement packets without payload
 * The maximum packet length is 263 bytes for full payload
 * 
 * @author ghelle
 *
 */
public class MAVLinkPacket implements Serializable {
	private static final long serialVersionUID = 2095947771227815314L;
	
	public static final int MAVLINK_STX = 254;
	
	/**
	 * Message length. NOT counting STX, LENGTH, SEQ, SYSID, COMPID, MSGID, CRC1 and CRC2
	 */
	public int len;
	/**
	 * Message sequence
	 */
	public int seq;
	/**
	 * ID of the SENDING system. Allows to differentiate different MAVs on the
	 * same network.
	 */
	public int sysid;
	/**
	 * ID of the SENDING component. Allows to differentiate different components
	 * of the same system, e.g. the IMU and the autopilot.
	 */
	public int compid;
	/**
	 * ID of the message - the id defines what the payload means and how it
	 * should be correctly decoded.
	 */
	public int msgid;
	/**
	 * Data of the message, depends on the message id.
	 */
	public MAVLinkPayload payload;
	/**
	 * ITU X.25/SAE AS-4 hash, excluding packet start sign, so bytes 1..(n+6)
	 * Note: The checksum also includes MAVLINK_CRC_EXTRA (Number computed from
	 * message fields. Protects the packet from decoding a different version of
	 * the same packet but with different variables).
	 */
	public CRC crc;	
	
	public MAVLinkPacket(){
		payload = new MAVLinkPayload();
	}
	
	/**
	 * Check if the size of the Payload is equal to the "len" byte
	 */
	public boolean payloadIsFilled() {
		return (payload.size() == len);
	}
	
	/**
	 * Update CRC for this packet.
	 */
	public void generateCRC(){
		crc = new CRC();
		crc.update_checksum(len);
		crc.update_checksum(seq);
		crc.update_checksum(sysid);
		crc.update_checksum(compid);
		crc.update_checksum(msgid);
		payload.resetIndex();
		for (int i = 0; i < payload.size(); i++) {
			crc.update_checksum(payload.getByte());
		}
		crc.finish_checksum(msgid);
    }

	/**
	 * Encode this packet for transmission. 
	 * 
	 * @return Array with bytes to be transmitted
	 */
	public byte[] encodePacket() {
		byte[] buffer = new byte[6 + len + 2];
		int i = 0;
		buffer[i++] = (byte) MAVLINK_STX;
		buffer[i++] = (byte) len;
		buffer[i++] = (byte) seq;
		buffer[i++] = (byte) sysid;
		buffer[i++] = (byte) compid;
		buffer[i++] = (byte) msgid;
		for (byte b : payload.payload) {
			buffer[i++] = b;
		}
		generateCRC();
		buffer[i++] = (byte) (crc.getLSB());
		buffer[i++] = (byte) (crc.getMSB());
		return buffer;
	}
	
	/**
	 * Unpack the data in this packet and return a MAVLink message
	 * 
	 * @return MAVLink message decoded from this packet
	 */
	public MAVLinkMessage unpack() {
		switch (msgid) {
		case msg_sensor_offsets.MAVLINK_MSG_ID_SENSOR_OFFSETS:
			return  new msg_sensor_offsets(payload);
		case msg_set_mag_offsets.MAVLINK_MSG_ID_SET_MAG_OFFSETS:
			return  new msg_set_mag_offsets(payload);
		case msg_meminfo.MAVLINK_MSG_ID_MEMINFO:
			return  new msg_meminfo(payload);
		case msg_ap_adc.MAVLINK_MSG_ID_AP_ADC:
			return  new msg_ap_adc(payload);
		case msg_digicam_configure.MAVLINK_MSG_ID_DIGICAM_CONFIGURE:
			return  new msg_digicam_configure(payload);
		case msg_digicam_control.MAVLINK_MSG_ID_DIGICAM_CONTROL:
			return  new msg_digicam_control(payload);
		case msg_mount_configure.MAVLINK_MSG_ID_MOUNT_CONFIGURE:
			return  new msg_mount_configure(payload);
		case msg_mount_control.MAVLINK_MSG_ID_MOUNT_CONTROL:
			return  new msg_mount_control(payload);
		case msg_mount_status.MAVLINK_MSG_ID_MOUNT_STATUS:
			return  new msg_mount_status(payload);
		case msg_fence_point.MAVLINK_MSG_ID_FENCE_POINT:
			return  new msg_fence_point(payload);
		case msg_fence_fetch_point.MAVLINK_MSG_ID_FENCE_FETCH_POINT:
			return  new msg_fence_fetch_point(payload);
		case msg_fence_status.MAVLINK_MSG_ID_FENCE_STATUS:
			return  new msg_fence_status(payload);
		case msg_ahrs.MAVLINK_MSG_ID_AHRS:
			return  new msg_ahrs(payload);
		case msg_simstate.MAVLINK_MSG_ID_SIMSTATE:
			return  new msg_simstate(payload);
		case msg_hwstatus.MAVLINK_MSG_ID_HWSTATUS:
			return  new msg_hwstatus(payload);
		case msg_radio.MAVLINK_MSG_ID_RADIO:
			return  new msg_radio(payload);
		case msg_limits_status.MAVLINK_MSG_ID_LIMITS_STATUS:
			return  new msg_limits_status(payload);
		case msg_wind.MAVLINK_MSG_ID_WIND:
			return  new msg_wind(payload);
		case msg_data16.MAVLINK_MSG_ID_DATA16:
			return  new msg_data16(payload);
		case msg_data32.MAVLINK_MSG_ID_DATA32:
			return  new msg_data32(payload);
		case msg_data64.MAVLINK_MSG_ID_DATA64:
			return  new msg_data64(payload);
		case msg_data96.MAVLINK_MSG_ID_DATA96:
			return  new msg_data96(payload);
		case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
			return  new msg_heartbeat(payload);
		case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
			return  new msg_sys_status(payload);
		case msg_system_time.MAVLINK_MSG_ID_SYSTEM_TIME:
			return  new msg_system_time(payload);
		case msg_ping.MAVLINK_MSG_ID_PING:
			return  new msg_ping(payload);
		case msg_change_operator_control.MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL:
			return  new msg_change_operator_control(payload);
		case msg_change_operator_control_ack.MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL_ACK:
			return  new msg_change_operator_control_ack(payload);
		case msg_auth_key.MAVLINK_MSG_ID_AUTH_KEY:
			return  new msg_auth_key(payload);
		case msg_set_mode.MAVLINK_MSG_ID_SET_MODE:
			return  new msg_set_mode(payload);
		case msg_param_request_read.MAVLINK_MSG_ID_PARAM_REQUEST_READ:
			return  new msg_param_request_read(payload);
		case msg_param_request_list.MAVLINK_MSG_ID_PARAM_REQUEST_LIST:
			return  new msg_param_request_list(payload);
		case msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE:
			return  new msg_param_value(payload);
		case msg_param_set.MAVLINK_MSG_ID_PARAM_SET:
			return  new msg_param_set(payload);
		case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
			return  new msg_gps_raw_int(payload);
		case msg_gps_status.MAVLINK_MSG_ID_GPS_STATUS:
			return  new msg_gps_status(payload);
		case msg_scaled_imu.MAVLINK_MSG_ID_SCALED_IMU:
			return  new msg_scaled_imu(payload);
		case msg_raw_imu.MAVLINK_MSG_ID_RAW_IMU:
			return  new msg_raw_imu(payload);
		case msg_raw_pressure.MAVLINK_MSG_ID_RAW_PRESSURE:
			return  new msg_raw_pressure(payload);
		case msg_scaled_pressure.MAVLINK_MSG_ID_SCALED_PRESSURE:
			return  new msg_scaled_pressure(payload);
		case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
			return  new msg_attitude(payload);
		case msg_attitude_quaternion.MAVLINK_MSG_ID_ATTITUDE_QUATERNION:
			return  new msg_attitude_quaternion(payload);
		case msg_local_position_ned.MAVLINK_MSG_ID_LOCAL_POSITION_NED:
			return  new msg_local_position_ned(payload);
		case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
			return  new msg_global_position_int(payload);
		case msg_rc_channels_scaled.MAVLINK_MSG_ID_RC_CHANNELS_SCALED:
			return  new msg_rc_channels_scaled(payload);
		case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:
			return  new msg_rc_channels_raw(payload);
		case msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW:
			return  new msg_servo_output_raw(payload);
		case msg_mission_request_partial_list.MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST:
			return  new msg_mission_request_partial_list(payload);
		case msg_mission_write_partial_list.MAVLINK_MSG_ID_MISSION_WRITE_PARTIAL_LIST:
			return  new msg_mission_write_partial_list(payload);
		case msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM:
			return  new msg_mission_item(payload);
		case msg_mission_request.MAVLINK_MSG_ID_MISSION_REQUEST:
			return  new msg_mission_request(payload);
		case msg_mission_set_current.MAVLINK_MSG_ID_MISSION_SET_CURRENT:
			return  new msg_mission_set_current(payload);
		case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
			return  new msg_mission_current(payload);
		case msg_mission_request_list.MAVLINK_MSG_ID_MISSION_REQUEST_LIST:
			return  new msg_mission_request_list(payload);
		case msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT:
			return  new msg_mission_count(payload);
		case msg_mission_clear_all.MAVLINK_MSG_ID_MISSION_CLEAR_ALL:
			return  new msg_mission_clear_all(payload);
		case msg_mission_item_reached.MAVLINK_MSG_ID_MISSION_ITEM_REACHED:
			return  new msg_mission_item_reached(payload);
		case msg_mission_ack.MAVLINK_MSG_ID_MISSION_ACK:
			return  new msg_mission_ack(payload);
		case msg_set_gps_global_origin.MAVLINK_MSG_ID_SET_GPS_GLOBAL_ORIGIN:
			return  new msg_set_gps_global_origin(payload);
		case msg_gps_global_origin.MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN:
			return  new msg_gps_global_origin(payload);
		case msg_set_local_position_setpoint.MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT:
			return  new msg_set_local_position_setpoint(payload);
		case msg_local_position_setpoint.MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT:
			return  new msg_local_position_setpoint(payload);
		case msg_global_position_setpoint_int.MAVLINK_MSG_ID_GLOBAL_POSITION_SETPOINT_INT:
			return  new msg_global_position_setpoint_int(payload);
		case msg_set_global_position_setpoint_int.MAVLINK_MSG_ID_SET_GLOBAL_POSITION_SETPOINT_INT:
			return  new msg_set_global_position_setpoint_int(payload);
		case msg_safety_set_allowed_area.MAVLINK_MSG_ID_SAFETY_SET_ALLOWED_AREA:
			return  new msg_safety_set_allowed_area(payload);
		case msg_safety_allowed_area.MAVLINK_MSG_ID_SAFETY_ALLOWED_AREA:
			return  new msg_safety_allowed_area(payload);
		case msg_set_roll_pitch_yaw_thrust.MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST:
			return  new msg_set_roll_pitch_yaw_thrust(payload);
		case msg_set_roll_pitch_yaw_speed_thrust.MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_SPEED_THRUST:
			return  new msg_set_roll_pitch_yaw_speed_thrust(payload);
		case msg_roll_pitch_yaw_thrust_setpoint.MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT:
			return  new msg_roll_pitch_yaw_thrust_setpoint(payload);
		case msg_roll_pitch_yaw_speed_thrust_setpoint.MAVLINK_MSG_ID_ROLL_PITCH_YAW_SPEED_THRUST_SETPOINT:
			return  new msg_roll_pitch_yaw_speed_thrust_setpoint(payload);
		case msg_set_quad_motors_setpoint.MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT:
			return  new msg_set_quad_motors_setpoint(payload);
		case msg_set_quad_swarm_roll_pitch_yaw_thrust.MAVLINK_MSG_ID_SET_QUAD_SWARM_ROLL_PITCH_YAW_THRUST:
			return  new msg_set_quad_swarm_roll_pitch_yaw_thrust(payload);
		case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
			return  new msg_nav_controller_output(payload);
		case msg_set_quad_swarm_led_roll_pitch_yaw_thrust.MAVLINK_MSG_ID_SET_QUAD_SWARM_LED_ROLL_PITCH_YAW_THRUST:
			return  new msg_set_quad_swarm_led_roll_pitch_yaw_thrust(payload);
		case msg_state_correction.MAVLINK_MSG_ID_STATE_CORRECTION:
			return  new msg_state_correction(payload);
		case msg_request_data_stream.MAVLINK_MSG_ID_REQUEST_DATA_STREAM:
			return  new msg_request_data_stream(payload);
		case msg_data_stream.MAVLINK_MSG_ID_DATA_STREAM:
			return  new msg_data_stream(payload);
		case msg_manual_control.MAVLINK_MSG_ID_MANUAL_CONTROL:
			return  new msg_manual_control(payload);
		case msg_rc_channels_override.MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE:
			return  new msg_rc_channels_override(payload);
		case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
			return  new msg_vfr_hud(payload);
		case msg_command_long.MAVLINK_MSG_ID_COMMAND_LONG:
			return  new msg_command_long(payload);
		case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:
			return  new msg_command_ack(payload);
		case msg_roll_pitch_yaw_rates_thrust_setpoint.MAVLINK_MSG_ID_ROLL_PITCH_YAW_RATES_THRUST_SETPOINT:
			return  new msg_roll_pitch_yaw_rates_thrust_setpoint(payload);
		case msg_manual_setpoint.MAVLINK_MSG_ID_MANUAL_SETPOINT:
			return  new msg_manual_setpoint(payload);
		case msg_local_position_ned_system_global_offset.MAVLINK_MSG_ID_LOCAL_POSITION_NED_SYSTEM_GLOBAL_OFFSET:
			return  new msg_local_position_ned_system_global_offset(payload);
		case msg_hil_state.MAVLINK_MSG_ID_HIL_STATE:
			return  new msg_hil_state(payload);
		case msg_hil_controls.MAVLINK_MSG_ID_HIL_CONTROLS:
			return  new msg_hil_controls(payload);
		case msg_hil_rc_inputs_raw.MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW:
			return  new msg_hil_rc_inputs_raw(payload);
		case msg_optical_flow.MAVLINK_MSG_ID_OPTICAL_FLOW:
			return  new msg_optical_flow(payload);
		case msg_global_vision_position_estimate.MAVLINK_MSG_ID_GLOBAL_VISION_POSITION_ESTIMATE:
			return  new msg_global_vision_position_estimate(payload);
		case msg_vision_position_estimate.MAVLINK_MSG_ID_VISION_POSITION_ESTIMATE:
			return  new msg_vision_position_estimate(payload);
		case msg_vision_speed_estimate.MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE:
			return  new msg_vision_speed_estimate(payload);
		case msg_vicon_position_estimate.MAVLINK_MSG_ID_VICON_POSITION_ESTIMATE:
			return  new msg_vicon_position_estimate(payload);
		case msg_highres_imu.MAVLINK_MSG_ID_HIGHRES_IMU:
			return  new msg_highres_imu(payload);
		case msg_file_transfer_start.MAVLINK_MSG_ID_FILE_TRANSFER_START:
			return  new msg_file_transfer_start(payload);
		case msg_file_transfer_dir_list.MAVLINK_MSG_ID_FILE_TRANSFER_DIR_LIST:
			return  new msg_file_transfer_dir_list(payload);
		case msg_file_transfer_res.MAVLINK_MSG_ID_FILE_TRANSFER_RES:
			return  new msg_file_transfer_res(payload);
		case msg_battery_status.MAVLINK_MSG_ID_BATTERY_STATUS:
			return  new msg_battery_status(payload);
		case msg_setpoint_8dof.MAVLINK_MSG_ID_SETPOINT_8DOF:
			return  new msg_setpoint_8dof(payload);
		case msg_setpoint_6dof.MAVLINK_MSG_ID_SETPOINT_6DOF:
			return  new msg_setpoint_6dof(payload);
		case msg_memory_vect.MAVLINK_MSG_ID_MEMORY_VECT:
			return  new msg_memory_vect(payload);
		case msg_debug_vect.MAVLINK_MSG_ID_DEBUG_VECT:
			return  new msg_debug_vect(payload);
		case msg_named_value_float.MAVLINK_MSG_ID_NAMED_VALUE_FLOAT:
			return  new msg_named_value_float(payload);
		case msg_named_value_int.MAVLINK_MSG_ID_NAMED_VALUE_INT:
			return  new msg_named_value_int(payload);
		case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:
			return  new msg_statustext(payload);
		case msg_debug.MAVLINK_MSG_ID_DEBUG:
			return  new msg_debug(payload);
		default:
			Log.d("MAVLink", "UNKNOW MESSAGE - " + msgid);
			return null;
		}
	}

}
	
