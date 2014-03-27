// MESSAGE LOCAL_POSITION_SETPOINT PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Transmit the current local setpoint of the controller to other MAVs (collision avoidance) and to the GCS.
*/
public class msg_local_position_setpoint extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT = 51;
	public static final int MAVLINK_MSG_LENGTH = 17;
	private static final long serialVersionUID = MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT;
	

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
		packet.msgid = MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT;
		packet.payload.putFloat(x);
		packet.payload.putFloat(y);
		packet.payload.putFloat(z);
		packet.payload.putFloat(yaw);
		packet.payload.putByte(coordinate_frame);
		return packet;		
	}

    /**
     * Decode a local_position_setpoint message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    x = payload.getFloat();
	    y = payload.getFloat();
	    z = payload.getFloat();
	    yaw = payload.getFloat();
	    coordinate_frame = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_local_position_setpoint(){
    	msgid = MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_local_position_setpoint(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "LOCAL_POSITION_SETPOINT");
        //Log.d("MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT", toString());
    }
    
          
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT -"+" x:"+x+" y:"+y+" z:"+z+" yaw:"+yaw+" coordinate_frame:"+coordinate_frame+"";
    }
}
