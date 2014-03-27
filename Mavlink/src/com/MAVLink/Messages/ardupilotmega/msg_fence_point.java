// MESSAGE FENCE_POINT PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* A fence point. Used to set a point when from
	      GCS -> MAV. Also used to return a point from MAV -> GCS
*/
public class msg_fence_point extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_FENCE_POINT = 160;
	public static final int MAVLINK_MSG_LENGTH = 12;
	private static final long serialVersionUID = MAVLINK_MSG_ID_FENCE_POINT;
	

 	/**
	* Latitude of point
	*/
	public float lat; 
 	/**
	* Longitude of point
	*/
	public float lng; 
 	/**
	* System ID
	*/
	public byte target_system; 
 	/**
	* Component ID
	*/
	public byte target_component; 
 	/**
	* point index (first point is 1, 0 is for return point)
	*/
	public byte idx; 
 	/**
	* total number of points (for sanity checking)
	*/
	public byte count; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_FENCE_POINT;
		packet.payload.putFloat(lat);
		packet.payload.putFloat(lng);
		packet.payload.putByte(target_system);
		packet.payload.putByte(target_component);
		packet.payload.putByte(idx);
		packet.payload.putByte(count);
		return packet;		
	}

    /**
     * Decode a fence_point message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    lat = payload.getFloat();
	    lng = payload.getFloat();
	    target_system = payload.getByte();
	    target_component = payload.getByte();
	    idx = payload.getByte();
	    count = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_fence_point(){
    	msgid = MAVLINK_MSG_ID_FENCE_POINT;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_fence_point(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_FENCE_POINT;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "FENCE_POINT");
        //Log.d("MAVLINK_MSG_ID_FENCE_POINT", toString());
    }
    
            
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_FENCE_POINT -"+" lat:"+lat+" lng:"+lng+" target_system:"+target_system+" target_component:"+target_component+" idx:"+idx+" count:"+count+"";
    }
}
