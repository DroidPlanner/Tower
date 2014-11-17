// MESSAGE SET_LOCAL_POSITION_SETPOINT PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Set the setpoint for a local position controller. This is the position in local coordinates the MAV should fly to. This message is sent by the path/MISSION planner to the onboard position controller. As some MAVs have a degree of freedom in yaw (e.g. all helicopters/quadrotors), the desired yaw angle is part of the message.
*/
public class msg_set_local_position_setpoint extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT = 50;
	public static final int MAVLINK_MSG_LENGTH = 19;
	private static final long serialVersionUID = MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT;
	

 	/**
	* x position
	*/
	public float x; 
 	/**
	* y position
	*/
	public float y; 
 	/**
	* z position
	*/
	public float z; 
 	/**
	* Desired yaw angle
	*/
	public float yaw; 
 	/**
	* System ID
	*/
	public byte target_system; 
 	/**
	* Component ID
	*/
	public byte target_component; 
 	/**
	* Coordinate frame - valid values are only MAV_FRAME_LOCAL_NED or MAV_FRAME_LOCAL_ENU
	*/
	public byte coordinate_frame; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT;
		packet.payload.putFloat(x);
		packet.payload.putFloat(y);
		packet.payload.putFloat(z);
		packet.payload.putFloat(yaw);
		packet.payload.putByte(target_system);
		packet.payload.putByte(target_component);
		packet.payload.putByte(coordinate_frame);
		return packet;		
	}

    /**
     * Decode a set_local_position_setpoint message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    x = payload.getFloat();
	    y = payload.getFloat();
	    z = payload.getFloat();
	    yaw = payload.getFloat();
	    target_system = payload.getByte();
	    target_component = payload.getByte();
	    coordinate_frame = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_set_local_position_setpoint(){
    	msgid = MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_set_local_position_setpoint(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "SET_LOCAL_POSITION_SETPOINT");
        //Log.d("MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT", toString());
    }
    
              
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT -"+" x:"+x+" y:"+y+" z:"+z+" yaw:"+yaw+" target_system:"+target_system+" target_component:"+target_component+" coordinate_frame:"+coordinate_frame+"";
    }
}
