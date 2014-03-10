// MESSAGE MISSION_REQUEST_PARTIAL_LIST PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Request a partial list of mission items from the system/component. http://qgroundcontrol.org/mavlink/waypoint_protocol. If start and end index are the same, just send one waypoint.
*/
public class msg_mission_request_partial_list extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST = 37;
	public static final int MAVLINK_MSG_LENGTH = 6;
	private static final long serialVersionUID = MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST;
	

 	/**
	* Start index, 0 by default
	*/
	public short start_index; 
 	/**
	* End index, -1 by default (-1: send list to end). Else a valid index of the list
	*/
	public short end_index; 
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
		packet.msgid = MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST;
		packet.payload.putShort(start_index);
		packet.payload.putShort(end_index);
		packet.payload.putByte(target_system);
		packet.payload.putByte(target_component);
		return packet;		
	}

    /**
     * Decode a mission_request_partial_list message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    start_index = payload.getShort();
	    end_index = payload.getShort();
	    target_system = payload.getByte();
	    target_component = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_mission_request_partial_list(){
    	msgid = MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_mission_request_partial_list(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "MISSION_REQUEST_PARTIAL_LIST");
        //Log.d("MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST", toString());
    }
    
        
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST -"+" start_index:"+start_index+" end_index:"+end_index+" target_system:"+target_system+" target_component:"+target_component+"";
    }
}
