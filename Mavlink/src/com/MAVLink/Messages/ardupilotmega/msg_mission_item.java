// MESSAGE MISSION_ITEM PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.MAVLinkPacket;
//import android.util.Log;

/**
* Message encoding a mission item. This message is emitted to announce
                the presence of a mission item and to set a mission item on the system. The mission item can be either in x, y, z meters (type: LOCAL) or x:lat, y:lon, z:altitude. Local frame is Z-down, right handed (NED), global frame is Z-up, right handed (ENU). See also http://qgroundcontrol.org/mavlink/waypoint_protocol.
*/
public class msg_mission_item extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_MISSION_ITEM = 39;
	public static final int MAVLINK_MSG_LENGTH = 37;
	private static final long serialVersionUID = MAVLINK_MSG_ID_MISSION_ITEM;
	

 	/**
	* PARAM1 / For NAV command MISSIONs: Radius in which the MISSION is accepted as reached, in meters
	*/
	public float param1; 
 	/**
	* PARAM2 / For NAV command MISSIONs: Time that the MAV should stay inside the PARAM1 radius before advancing, in milliseconds
	*/
	public float param2; 
 	/**
	* PARAM3 / For LOITER command MISSIONs: Orbit to circle around the MISSION, in meters. If positive the orbit direction should be clockwise, if negative the orbit direction should be counter-clockwise.
	*/
	public float param3; 
 	/**
	* PARAM4 / For NAV and LOITER command MISSIONs: Yaw orientation in degrees, [0..360] 0 = NORTH
	*/
	public float param4; 
 	/**
	* PARAM5 / local: x position, global: latitude
	*/
	public float x; 
 	/**
	* PARAM6 / y position: global: longitude
	*/
	public float y; 
 	/**
	* PARAM7 / z position: global: altitude
	*/
	public float z; 
 	/**
	* Sequence
	*/
	public short seq; 
 	/**
	* The scheduled action for the MISSION. see MAV_CMD in common.xml MAVLink specs
	*/
	public short command; 
 	/**
	* System ID
	*/
	public byte target_system; 
 	/**
	* Component ID
	*/
	public byte target_component; 
 	/**
	* The coordinate system of the MISSION. see MAV_FRAME in mavlink_types.h
	*/
	public byte frame; 
 	/**
	* false:0, true:1
	*/
	public byte current; 
 	/**
	* autocontinue to next wp
	*/
	public byte autocontinue; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_MISSION_ITEM;
		packet.payload.putFloat(param1);
		packet.payload.putFloat(param2);
		packet.payload.putFloat(param3);
		packet.payload.putFloat(param4);
		packet.payload.putFloat(x);
		packet.payload.putFloat(y);
		packet.payload.putFloat(z);
		packet.payload.putShort(seq);
		packet.payload.putShort(command);
		packet.payload.putByte(target_system);
		packet.payload.putByte(target_component);
		packet.payload.putByte(frame);
		packet.payload.putByte(current);
		packet.payload.putByte(autocontinue);
		return packet;		
	}

    /**
     * Decode a mission_item message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    param1 = payload.getFloat();
	    param2 = payload.getFloat();
	    param3 = payload.getFloat();
	    param4 = payload.getFloat();
	    x = payload.getFloat();
	    y = payload.getFloat();
	    z = payload.getFloat();
	    seq = payload.getShort();
	    command = payload.getShort();
	    target_system = payload.getByte();
	    target_component = payload.getByte();
	    frame = payload.getByte();
	    current = payload.getByte();
	    autocontinue = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_mission_item(){
    	msgid = MAVLINK_MSG_ID_MISSION_ITEM;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_mission_item(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_MISSION_ITEM;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "MISSION_ITEM");
        //Log.d("MAVLINK_MSG_ID_MISSION_ITEM", toString());
    }
    
                            
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_MISSION_ITEM -"+" param1:"+param1+" param2:"+param2+" param3:"+param3+" param4:"+param4+" x:"+x+" y:"+y+" z:"+z+" seq:"+seq+" command:"+command+" target_system:"+target_system+" target_component:"+target_component+" frame:"+frame+" current:"+current+" autocontinue:"+autocontinue+"";
    }
}
