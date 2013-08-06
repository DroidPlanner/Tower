// MESSAGE FENCE_STATUS PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.MAVLinkPacket;
//import android.util.Log;

/**
* Status of geo-fencing. Sent in extended
	    status stream when fencing enabled
*/
public class msg_fence_status extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_FENCE_STATUS = 162;
	public static final int MAVLINK_MSG_LENGTH = 8;
	private static final long serialVersionUID = MAVLINK_MSG_ID_FENCE_STATUS;
	

 	/**
	* time of last breach in milliseconds since boot
	*/
	public int breach_time; 
 	/**
	* number of fence breaches
	*/
	public short breach_count; 
 	/**
	* 0 if currently inside fence, 1 if outside
	*/
	public byte breach_status; 
 	/**
	* last breach type (see FENCE_BREACH_* enum)
	*/
	public byte breach_type; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_FENCE_STATUS;
		packet.payload.putInt(breach_time);
		packet.payload.putShort(breach_count);
		packet.payload.putByte(breach_status);
		packet.payload.putByte(breach_type);
		return packet;		
	}

    /**
     * Decode a fence_status message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    breach_time = payload.getInt();
	    breach_count = payload.getShort();
	    breach_status = payload.getByte();
	    breach_type = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_fence_status(){
    	msgid = MAVLINK_MSG_ID_FENCE_STATUS;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_fence_status(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_FENCE_STATUS;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "FENCE_STATUS");
        //Log.d("MAVLINK_MSG_ID_FENCE_STATUS", toString());
    }
    
        
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_FENCE_STATUS -"+" breach_time:"+breach_time+" breach_count:"+breach_count+" breach_status:"+breach_status+" breach_type:"+breach_type+"";
    }
}
