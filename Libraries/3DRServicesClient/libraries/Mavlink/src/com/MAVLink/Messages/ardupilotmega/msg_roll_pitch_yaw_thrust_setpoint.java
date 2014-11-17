// MESSAGE ROLL_PITCH_YAW_THRUST_SETPOINT PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Setpoint in roll, pitch, yaw currently active on the system.
*/
public class msg_roll_pitch_yaw_thrust_setpoint extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT = 58;
	public static final int MAVLINK_MSG_LENGTH = 20;
	private static final long serialVersionUID = MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT;
	

 	/**
	* Timestamp in milliseconds since system boot
	*/
	public int time_boot_ms; 
 	/**
	* Desired roll angle in radians
	*/
	public float roll; 
 	/**
	* Desired pitch angle in radians
	*/
	public float pitch; 
 	/**
	* Desired yaw angle in radians
	*/
	public float yaw; 
 	/**
	* Collective thrust, normalized to 0 .. 1
	*/
	public float thrust; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT;
		packet.payload.putInt(time_boot_ms);
		packet.payload.putFloat(roll);
		packet.payload.putFloat(pitch);
		packet.payload.putFloat(yaw);
		packet.payload.putFloat(thrust);
		return packet;		
	}

    /**
     * Decode a roll_pitch_yaw_thrust_setpoint message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    time_boot_ms = payload.getInt();
	    roll = payload.getFloat();
	    pitch = payload.getFloat();
	    yaw = payload.getFloat();
	    thrust = payload.getFloat();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_roll_pitch_yaw_thrust_setpoint(){
    	msgid = MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_roll_pitch_yaw_thrust_setpoint(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "ROLL_PITCH_YAW_THRUST_SETPOINT");
        //Log.d("MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT", toString());
    }
    
          
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT -"+" time_boot_ms:"+time_boot_ms+" roll:"+roll+" pitch:"+pitch+" yaw:"+yaw+" thrust:"+thrust+"";
    }
}
