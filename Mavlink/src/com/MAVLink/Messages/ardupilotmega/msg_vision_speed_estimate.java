// MESSAGE VISION_SPEED_ESTIMATE PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.MAVLinkPacket;
//import android.util.Log;

/**
* 
*/
public class msg_vision_speed_estimate extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE = 103;
	public static final int MAVLINK_MSG_LENGTH = 20;
	private static final long serialVersionUID = MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE;
	

 	/**
	* Timestamp (microseconds, synced to UNIX time or since system boot)
	*/
	public long usec; 
 	/**
	* Global X speed
	*/
	public float x; 
 	/**
	* Global Y speed
	*/
	public float y; 
 	/**
	* Global Z speed
	*/
	public float z; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE;
		packet.payload.putLong(usec);
		packet.payload.putFloat(x);
		packet.payload.putFloat(y);
		packet.payload.putFloat(z);
		return packet;		
	}

    /**
     * Decode a vision_speed_estimate message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    usec = payload.getLong();
	    x = payload.getFloat();
	    y = payload.getFloat();
	    z = payload.getFloat();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_vision_speed_estimate(){
    	msgid = MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_vision_speed_estimate(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "VISION_SPEED_ESTIMATE");
        //Log.d("MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE", toString());
    }
    
        
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE -"+" usec:"+usec+" x:"+x+" y:"+y+" z:"+z+"";
    }
}
