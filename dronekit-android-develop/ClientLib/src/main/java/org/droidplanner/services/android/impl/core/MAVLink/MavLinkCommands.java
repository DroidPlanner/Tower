package org.droidplanner.services.android.impl.core.MAVLink;

import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_manual_control;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_set_mode;
import com.MAVLink.common.msg_set_position_target_global_int;
import com.MAVLink.common.msg_set_position_target_local_ned;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;
import com.MAVLink.enums.MAV_GOTO;
import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.variables.ApmModes;

public class MavLinkCommands {

    public static final int EMERGENCY_DISARM_MAGIC_NUMBER = 21196;

    private static final int MAVLINK_SET_POS_TYPE_MASK_POS_IGNORE = ((1 << 0) | (1 << 1) | (1 << 2));
    private static final int MAVLINK_SET_POS_TYPE_MASK_VEL_IGNORE = ((1 << 3) | (1 << 4) | (1 << 5));
    private static final int MAVLINK_SET_POS_TYPE_MASK_ACC_IGNORE = ((1 << 6) | (1 << 7) | (1 << 8));

    public static void changeMissionSpeed(MavLinkDrone drone, float speed, ICommandListener listener) {
        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = MAV_CMD.MAV_CMD_DO_CHANGE_SPEED;
        msg.param1 = 0; // TODO use correct parameter
        msg.param2 = speed;
        msg.param3 = 0; // TODO use correct parameter

        drone.getMavClient().sendMessage(msg, listener);
    }

    public static void setGuidedMode(MavLinkDrone drone, double latitude, double longitude, double d) {
        msg_mission_item msg = new msg_mission_item();
        msg.seq = 0;
        msg.current = 2; // TODO use guided mode enum
        msg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        msg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT; //
        msg.param1 = 0; // TODO use correct parameter
        msg.param2 = 0; // TODO use correct parameter
        msg.param3 = 0; // TODO use correct parameter
        msg.param4 = 0; // TODO use correct parameter
        msg.x = (float) latitude;
        msg.y = (float) longitude;
        msg.z = (float) d;
        msg.autocontinue = 1; // TODO use correct parameter
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        drone.getMavClient().sendMessage(msg, null);
    }

    public static void sendGuidedPosition(MavLinkDrone drone, double latitude, double longitude, double altitude){
        msg_set_position_target_global_int msg = new msg_set_position_target_global_int();
        msg.type_mask = MAVLINK_SET_POS_TYPE_MASK_ACC_IGNORE | MAVLINK_SET_POS_TYPE_MASK_VEL_IGNORE;
        msg.coordinate_frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT_INT;
        msg.lat_int = (int) (latitude * 1E7);
        msg.lon_int = (int) (longitude * 1E7);
        msg.alt = (float) altitude;
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        drone.getMavClient().sendMessage(msg, null);
    }

    public static void sendGuidedVelocity(MavLinkDrone drone, double xVel, double yVel, double zVel){
        msg_set_position_target_global_int msg = new msg_set_position_target_global_int();
        msg.type_mask = MAVLINK_SET_POS_TYPE_MASK_ACC_IGNORE | MAVLINK_SET_POS_TYPE_MASK_POS_IGNORE;
        msg.coordinate_frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT_INT;
        msg.vx = (float) xVel;
        msg.vy = (float) yVel;
        msg.vz = (float) zVel;
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        drone.getMavClient().sendMessage(msg, null);
    }

    public static void setVelocityInLocalFrame(MavLinkDrone drone, float xVel, float yVel, float zVel, ICommandListener listener){
        msg_set_position_target_local_ned msg = new msg_set_position_target_local_ned();
        msg.type_mask = MAVLINK_SET_POS_TYPE_MASK_ACC_IGNORE | MAVLINK_SET_POS_TYPE_MASK_POS_IGNORE;
        msg.vx = xVel;
        msg.vy = yVel;
        msg.vz = zVel;
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        drone.getMavClient().sendMessage(msg, listener);
    }

    public static void sendGuidedPositionAndVelocity(MavLinkDrone drone, double latitude, double longitude, double altitude,
                                                     double xVel, double yVel, double zVel){
        msg_set_position_target_global_int msg = new msg_set_position_target_global_int();
        msg.type_mask = MAVLINK_SET_POS_TYPE_MASK_ACC_IGNORE;
        msg.coordinate_frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT_INT;
        msg.lat_int = (int) (latitude * 1E7);
        msg.lon_int = (int) (longitude * 1E7);
        msg.alt = (float) altitude;
        msg.vx = (float) xVel;
        msg.vy = (float) yVel;
        msg.vz = (float) zVel;
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        drone.getMavClient().sendMessage(msg, null);
    }

    public static void changeFlightMode(MavLinkDrone drone, ApmModes mode, ICommandListener listener) {
        msg_set_mode msg = new msg_set_mode();
        msg.target_system = drone.getSysid();
        msg.base_mode = 1; // TODO use meaningful constant
        msg.custom_mode = mode.getNumber();
        drone.getMavClient().sendMessage(msg, listener);
    }

    public static void setConditionYaw(MavLinkDrone drone, float targetAngle, float yawRate, boolean isClockwise,
                                       boolean isRelative, ICommandListener listener){
        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();

        msg.command = MAV_CMD.MAV_CMD_CONDITION_YAW;
        msg.param1 = targetAngle;
        msg.param2 = yawRate;
        msg.param3 = isClockwise ? 1 : -1;
        msg.param4 = isRelative ? 1 : 0;

        drone.getMavClient().sendMessage(msg, listener);
    }

    /**
     * API for sending manually control to the vehicle using standard joystick axes nomenclature, along with a joystick-like input device.
     * Unused axes can be disabled and buttons are also transmit as boolean values.
     * @see <a href="MANUAL_CONTROL">https://pixhawk.ethz.ch/mavlink/#MANUAL_CONTROL</a>
     *
     * @param drone
     * @param x X-axis, normalized to the range [-1000,1000]. A value of INT16_MAX indicates that this axis is invalid. Generally corresponds to forward(1000)-backward(-1000) movement on a joystick and the pitch of a vehicle.
     * @param y Y-axis, normalized to the range [-1000,1000]. A value of INT16_MAX indicates that this axis is invalid. Generally corresponds to left(-1000)-right(1000) movement on a joystick and the roll of a vehicle.
     * @param z Z-axis, normalized to the range [-1000,1000]. A value of INT16_MAX indicates that this axis is invalid. Generally corresponds to a separate slider movement with maximum being 1000 and minimum being -1000 on a joystick and the thrust of a vehicle.
     * @param r R-axis, normalized to the range [-1000,1000]. A value of INT16_MAX indicates that this axis is invalid. Generally corresponds to a twisting of the joystick, with counter-clockwise being 1000 and clockwise being -1000, and the yaw of a vehicle.
     * @param buttons A bitfield corresponding to the joystick buttons' current state, 1 for pressed, 0 for released. The lowest bit corresponds to Button 1.
     * @param listener
     */
    public static void sendManualControl(MavLinkDrone drone, short x, short y, short z, short r, int buttons, ICommandListener listener){
        msg_manual_control msg = new msg_manual_control();
        msg.target = drone.getSysid();
        msg.x = x;
        msg.y = y;
        msg.z = z;
        msg.r = r;
        msg.buttons = buttons;
        drone.getMavClient().sendMessage(msg, listener);
    }

    public static void sendTakeoff(MavLinkDrone drone, double alt, ICommandListener listener) {
        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;

        msg.param7 = (float) alt;

        drone.getMavClient().sendMessage(msg, listener);
    }

    public static void sendNavLand(MavLinkDrone drone, ICommandListener listener){
        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = MAV_CMD.MAV_CMD_NAV_LAND;

        drone.getMavClient().sendMessage(msg, listener);
    }

    public static void sendNavRTL(MavLinkDrone drone, ICommandListener listener){
        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH;

        drone.getMavClient().sendMessage(msg, listener);
    }

    public static void sendPause(MavLinkDrone drone, ICommandListener listener){
        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();

        msg.command = MAV_CMD.MAV_CMD_OVERRIDE_GOTO;
        msg.param1 = MAV_GOTO.MAV_GOTO_DO_HOLD;
        msg.param2 = MAV_GOTO.MAV_GOTO_HOLD_AT_CURRENT_POSITION;

        drone.getMavClient().sendMessage(msg, listener);
    }

    public static void startMission(MavLinkDrone drone, ICommandListener listener){
        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = MAV_CMD.MAV_CMD_MISSION_START;

        drone.getMavClient().sendMessage(msg, listener);
    }

    public static void sendArmMessage(MavLinkDrone drone, boolean arm, boolean emergencyDisarm, ICommandListener listener) {
        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();

        msg.command = MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM;
        msg.param1 = arm ? 1 : 0;
        msg.param2 = emergencyDisarm ? EMERGENCY_DISARM_MAGIC_NUMBER : 0;
        msg.param3 = 0;
        msg.param4 = 0;
        msg.param5 = 0;
        msg.param6 = 0;
        msg.param7 = 0;
        msg.confirmation = 0;
        drone.getMavClient().sendMessage(msg, listener);
    }

    public static void sendFlightTermination(MavLinkDrone drone, ICommandListener listener) {
        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();

        msg.command = MAV_CMD.MAV_CMD_DO_FLIGHTTERMINATION;
        msg.param1 = 1;

        drone.getMavClient().sendMessage(msg, listener);
    }
}
