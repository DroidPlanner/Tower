// MESSAGE OPTICAL_FLOW PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Optical flow from a flow sensor (e.g. optical mouse sensor)
*/
public class msg_optical_flow extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_OPTICAL_FLOW = 100;
	public static final int MAVLINK_MSG_LENGTH = 26;
	private static final long serialVersionUID = MAVLINK_MSG_ID_OPTICAL_FLOW;
	

 	/**
	* Timestamp (UNIX)
	*/
	public long time_usec; 
 	/**
	* Flow in meters in x-sensor direction, angular-speed compensated
	*/
	public float flow_comp_m_x; 
 	/**
	* Flow in meters in y-sensor direction, angular-speed compensated
	*/
	public float flow_comp_m_y; 
 	/**
	* Ground distance in meters. Positive value: distance known. Negative value: Unknown distance
	*/
	public float ground_distance; 
 	/**
	* Flow in pixels * 10 in x-sensor direction (dezi-pixels)
	*/
	public short flow_x; 
 	/**
	* Flow in pixels * 10 in y-sensor direction (dezi-pixels)
	*/
	public short flow_y; 
 	/**
	* Sensor ID
	*/
	public byte sensor_id; 
 	/**
	* Optical flow quality / confidence. 0: bad, 255: maximum quality
	*/
	public byte quality; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_OPTICAL_FLOW;
		packet.payload.putLong(time_usec);
		packet.payload.putFloat(flow_comp_m_x);
		packet.payload.putFloat(flow_comp_m_y);
		packet.payload.putFloat(ground_distance);
		packet.payload.putShort(flow_x);
		packet.payload.putShort(flow_y);
		packet.payload.putByte(sensor_id);
		packet.payload.putByte(quality);
		return packet;		
	}

    /**
     * Decode a optical_flow message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    time_usec = payload.getLong();
	    flow_comp_m_x = payload.getFloat();
	    flow_comp_m_y = payload.getFloat();
	    ground_distance = payload.getFloat();
	    flow_x = payload.getShort();
	    flow_y = payload.getShort();
	    sensor_id = payload.getByte();
	    quality = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_optical_flow(){
    	msgid = MAVLINK_MSG_ID_OPTICAL_FLOW;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_optical_flow(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_OPTICAL_FLOW;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "OPTICAL_FLOW");
        //Log.d("MAVLINK_MSG_ID_OPTICAL_FLOW", toString());
    }
    
                
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_OPTICAL_FLOW -"+" time_usec:"+time_usec+" flow_comp_m_x:"+flow_comp_m_x+" flow_comp_m_y:"+flow_comp_m_y+" ground_distance:"+ground_distance+" flow_x:"+flow_x+" flow_y:"+flow_y+" sensor_id:"+sensor_id+" quality:"+quality+"";
    }
}
