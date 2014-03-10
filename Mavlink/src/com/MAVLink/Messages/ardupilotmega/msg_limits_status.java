// MESSAGE LIMITS_STATUS PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Status of AP_Limits. Sent in extended
	    status stream when AP_Limits is enabled
*/
public class msg_limits_status extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_LIMITS_STATUS = 167;
	public static final int MAVLINK_MSG_LENGTH = 22;
	private static final long serialVersionUID = MAVLINK_MSG_ID_LIMITS_STATUS;
	

 	/**
	* time of last breach in milliseconds since boot
	*/
	public int last_trigger; 
 	/**
	* time of last recovery action in milliseconds since boot
	*/
	public int last_action; 
 	/**
	* time of last successful recovery in milliseconds since boot
	*/
	public int last_recovery; 
 	/**
	* time of last all-clear in milliseconds since boot
	*/
	public int last_clear; 
 	/**
	* number of fence breaches
	*/
	public short breach_count; 
 	/**
	* state of AP_Limits, (see enum LimitState, LIMITS_STATE)
	*/
	public byte limits_state; 
 	/**
	* AP_Limit_Module bitfield of enabled modules, (see enum moduleid or LIMIT_MODULE)
	*/
	public byte mods_enabled; 
 	/**
	* AP_Limit_Module bitfield of required modules, (see enum moduleid or LIMIT_MODULE)
	*/
	public byte mods_required; 
 	/**
	* AP_Limit_Module bitfield of triggered modules, (see enum moduleid or LIMIT_MODULE)
	*/
	public byte mods_triggered; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_LIMITS_STATUS;
		packet.payload.putInt(last_trigger);
		packet.payload.putInt(last_action);
		packet.payload.putInt(last_recovery);
		packet.payload.putInt(last_clear);
		packet.payload.putShort(breach_count);
		packet.payload.putByte(limits_state);
		packet.payload.putByte(mods_enabled);
		packet.payload.putByte(mods_required);
		packet.payload.putByte(mods_triggered);
		return packet;		
	}

    /**
     * Decode a limits_status message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    last_trigger = payload.getInt();
	    last_action = payload.getInt();
	    last_recovery = payload.getInt();
	    last_clear = payload.getInt();
	    breach_count = payload.getShort();
	    limits_state = payload.getByte();
	    mods_enabled = payload.getByte();
	    mods_required = payload.getByte();
	    mods_triggered = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_limits_status(){
    	msgid = MAVLINK_MSG_ID_LIMITS_STATUS;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_limits_status(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_LIMITS_STATUS;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "LIMITS_STATUS");
        //Log.d("MAVLINK_MSG_ID_LIMITS_STATUS", toString());
    }
    
                  
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_LIMITS_STATUS -"+" last_trigger:"+last_trigger+" last_action:"+last_action+" last_recovery:"+last_recovery+" last_clear:"+last_clear+" breach_count:"+breach_count+" limits_state:"+limits_state+" mods_enabled:"+mods_enabled+" mods_required:"+mods_required+" mods_triggered:"+mods_triggered+"";
    }
}
