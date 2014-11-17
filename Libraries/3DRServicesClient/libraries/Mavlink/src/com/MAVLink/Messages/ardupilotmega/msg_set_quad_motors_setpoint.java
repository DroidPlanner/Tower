// MESSAGE SET_QUAD_MOTORS_SETPOINT PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Setpoint in the four motor speeds
*/
public class msg_set_quad_motors_setpoint extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT = 60;
	public static final int MAVLINK_MSG_LENGTH = 9;
	private static final long serialVersionUID = MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT;
	

 	/**
	* Front motor in + configuration, front left motor in x configuration
	*/
	public short motor_front_nw; 
 	/**
	* Right motor in + configuration, front right motor in x configuration
	*/
	public short motor_right_ne; 
 	/**
	* Back motor in + configuration, back right motor in x configuration
	*/
	public short motor_back_se; 
 	/**
	* Left motor in + configuration, back left motor in x configuration
	*/
	public short motor_left_sw; 
 	/**
	* System ID of the system that should set these motor commands
	*/
	public byte target_system; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT;
		packet.payload.putShort(motor_front_nw);
		packet.payload.putShort(motor_right_ne);
		packet.payload.putShort(motor_back_se);
		packet.payload.putShort(motor_left_sw);
		packet.payload.putByte(target_system);
		return packet;		
	}

    /**
     * Decode a set_quad_motors_setpoint message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    motor_front_nw = payload.getShort();
	    motor_right_ne = payload.getShort();
	    motor_back_se = payload.getShort();
	    motor_left_sw = payload.getShort();
	    target_system = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_set_quad_motors_setpoint(){
    	msgid = MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_set_quad_motors_setpoint(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "SET_QUAD_MOTORS_SETPOINT");
        //Log.d("MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT", toString());
    }
    
          
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT -"+" motor_front_nw:"+motor_front_nw+" motor_right_ne:"+motor_right_ne+" motor_back_se:"+motor_back_se+" motor_left_sw:"+motor_left_sw+" target_system:"+target_system+"";
    }
}
