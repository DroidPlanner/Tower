// MESSAGE LOG_ERASE PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Erase all logs
*/
public class msg_log_erase extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_LOG_ERASE = 121;
	public static final int MAVLINK_MSG_LENGTH = 2;
	private static final long serialVersionUID = MAVLINK_MSG_ID_LOG_ERASE;
	

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
		packet.msgid = MAVLINK_MSG_ID_LOG_ERASE;
		packet.payload.putByte(target_system);
		packet.payload.putByte(target_component);
		return packet;		
	}

    /**
     * Decode a log_erase message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    target_system = payload.getByte();
	    target_component = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_log_erase(){
    	msgid = MAVLINK_MSG_ID_LOG_ERASE;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_log_erase(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_LOG_ERASE;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "LOG_ERASE");
        //Log.d("MAVLINK_MSG_ID_LOG_ERASE", toString());
    }
    
    
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_LOG_ERASE -"+" target_system:"+target_system+" target_component:"+target_component+"";
    }
}
