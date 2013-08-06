// MESSAGE MOUNT_CONTROL PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.MAVLinkPacket;
//import android.util.Log;

/**
* Message to control a camera mount, directional antenna, etc.
*/
public class msg_mount_control extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_MOUNT_CONTROL = 157;
	public static final int MAVLINK_MSG_LENGTH = 15;
	private static final long serialVersionUID = MAVLINK_MSG_ID_MOUNT_CONTROL;
	

 	/**
	* pitch(deg*100) or lat, depending on mount mode
	*/
	public int input_a; 
 	/**
	* roll(deg*100) or lon depending on mount mode
	*/
	public int input_b; 
 	/**
	* yaw(deg*100) or alt (in cm) depending on mount mode
	*/
	public int input_c; 
 	/**
	* System ID
	*/
	public byte target_system; 
 	/**
	* Component ID
	*/
	public byte target_component; 
 	/**
	* if "1" it will save current trimmed position on EEPROM (just valid for NEUTRAL and LANDING)
	*/
	public byte save_position; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_MOUNT_CONTROL;
		packet.payload.putInt(input_a);
		packet.payload.putInt(input_b);
		packet.payload.putInt(input_c);
		packet.payload.putByte(target_system);
		packet.payload.putByte(target_component);
		packet.payload.putByte(save_position);
		return packet;		
	}

    /**
     * Decode a mount_control message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    input_a = payload.getInt();
	    input_b = payload.getInt();
	    input_c = payload.getInt();
	    target_system = payload.getByte();
	    target_component = payload.getByte();
	    save_position = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_mount_control(){
    	msgid = MAVLINK_MSG_ID_MOUNT_CONTROL;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_mount_control(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_MOUNT_CONTROL;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "MOUNT_CONTROL");
        //Log.d("MAVLINK_MSG_ID_MOUNT_CONTROL", toString());
    }
    
            
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_MOUNT_CONTROL -"+" input_a:"+input_a+" input_b:"+input_b+" input_c:"+input_c+" target_system:"+target_system+" target_component:"+target_component+" save_position:"+save_position+"";
    }
}
