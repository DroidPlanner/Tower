// MESSAGE SET_ROLL_PITCH_YAW_THRUST PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Set roll, pitch and yaw.
*/
public class msg_set_roll_pitch_yaw_thrust extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST = 56;
	public static final int MAVLINK_MSG_LENGTH = 18;
	private static final long serialVersionUID = MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST;
	

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
	* System ID
	*/
	public byte target_system; 
 	/**
	* Component ID
	*/
	public byte target_component; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST;
		packet.payload.putFloat(roll);
		packet.payload.putFloat(pitch);
		packet.payload.putFloat(yaw);
		packet.payload.putFloat(thrust);
		packet.payload.putByte(target_system);
		packet.payload.putByte(target_component);
		return packet;		
	}

    /**
     * Decode a set_roll_pitch_yaw_thrust message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    roll = payload.getFloat();
	    pitch = payload.getFloat();
	    yaw = payload.getFloat();
	    thrust = payload.getFloat();
	    target_system = payload.getByte();
	    target_component = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_set_roll_pitch_yaw_thrust(){
    	msgid = MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_set_roll_pitch_yaw_thrust(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "SET_ROLL_PITCH_YAW_THRUST");
        //Log.d("MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST", toString());
    }
    
            
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST -"+" roll:"+roll+" pitch:"+pitch+" yaw:"+yaw+" thrust:"+thrust+" target_system:"+target_system+" target_component:"+target_component+"";
    }
}
