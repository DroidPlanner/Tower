package org.droidplanner.services.android.impl.core.MAVLink.command.doCmd;

import com.MAVLink.ardupilotmega.msg_digicam_control;
import com.MAVLink.ardupilotmega.msg_mount_control;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_mission_set_current;
import com.MAVLink.enums.GRIPPER_ACTIONS;
import com.MAVLink.enums.MAV_CMD;

import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.model.ICommandListener;

public class MavLinkDoCmds {

    public static void setVehicleHome(MavLinkDrone drone, LatLongAlt location, ICommandListener listener){
        if(drone == null || location == null)
            return;

        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = MAV_CMD.MAV_CMD_DO_SET_HOME;

        msg.param5 = (float) location.getLatitude();
        msg.param6 = (float) location.getLongitude();
        msg.param7 = (float) location.getAltitude();

        drone.getMavClient().sendMessage(msg, listener);
    }

    public static void setROI(MavLinkDrone drone, LatLongAlt coord, ICommandListener listener) {
        if (drone == null)
            return;

        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = MAV_CMD.MAV_CMD_DO_SET_ROI;

        msg.param5 = (float) coord.getLatitude();
        msg.param6 = (float) coord.getLongitude();
        msg.param7 = (float) coord.getAltitude();

        drone.getMavClient().sendMessage(msg, listener);
    }

    public static void resetROI(MavLinkDrone drone, ICommandListener listener) {
        if (drone == null)
            return;

        setROI(drone, new LatLongAlt(0, 0, 0), listener);
    }

    public static void triggerCamera(MavLinkDrone drone) {
        if (drone == null)
            return;

        msg_digicam_control msg = new msg_digicam_control();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.shot = 1;
        drone.getMavClient().sendMessage(msg, null);
    }

    public static void empCommand(MavLinkDrone drone, boolean release, ICommandListener listener) {
        if (drone == null)
            return;

        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = MAV_CMD.MAV_CMD_DO_GRIPPER;
        msg.param2 = release ? GRIPPER_ACTIONS.GRIPPER_ACTION_RELEASE : GRIPPER_ACTIONS.GRIPPER_ACTION_GRAB;

        drone.getMavClient().sendMessage(msg, listener);
    }

    /**
     * Set a Relay pin’s voltage high or low
     *
     * @param drone       target vehicle
     * @param relayNumber
     * @param enabled     true for relay to be on, false for relay to be off.
     */
    public static void setRelay(MavLinkDrone drone, int relayNumber, boolean enabled, ICommandListener listener) {
        if (drone == null)
            return;

        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = MAV_CMD.MAV_CMD_DO_SET_RELAY;
        msg.param1 = relayNumber;
        msg.param2 = enabled ? 1 : 0;

        drone.getMavClient().sendMessage(msg, listener);
    }

    /**
     * Move a servo to a particular pwm value
     *
     * @param drone   target vehicle
     * @param channel he output channel the servo is attached to
     * @param pwm     PWM value to output to the servo. Servo’s generally accept pwm values between 1000 and 2000
     */
    public static void setServo(MavLinkDrone drone, int channel, int pwm, ICommandListener listener) {
        if (drone == null)
            return;

        msg_command_long msg = new msg_command_long();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.command = MAV_CMD.MAV_CMD_DO_SET_SERVO;
        msg.param1 = channel;
        msg.param2 = pwm;

        drone.getMavClient().sendMessage(msg, listener);
    }

    /**
     * Set the orientation of a gimbal
     *
     * @param drone    target vehicle
     * @param pitch    the desired gimbal pitch in degrees
     * @param roll     the desired gimbal roll in degrees
     * @param yaw      the desired gimbal yaw in degrees
     * @param listener Register a callback to receive update of the command execution state.
     */
    public static void setGimbalOrientation(MavLinkDrone drone, float pitch, float roll, float yaw, ICommandListener
            listener) {
        if (drone == null)
            return;

        msg_mount_control msg = new msg_mount_control();
        msg.target_system = drone.getSysid();
        msg.target_component = drone.getCompid();
        msg.input_a = (int) (pitch * 100);
        msg.input_b = (int) (roll * 100);
        msg.input_c = (int) (yaw * 100);

        drone.getMavClient().sendMessage(msg, listener);
    }

    /**
     * Jump to the desired command in the mission list. Repeat this action only the specified number of times
     *
     * @param drone    target vehicle
     * @param waypoint command
     * @param listener Register a callback to receive update of the command execution state.
     */
    public static void gotoWaypoint(MavLinkDrone drone, int waypoint, ICommandListener listener) {
        if (drone == null)
            return;
        msg_mission_set_current msg = new msg_mission_set_current();
        msg.seq = waypoint;
        drone.getMavClient().sendMessage(msg, listener);
    }

}
