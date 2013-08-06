// MESSAGE MOUNT_STATUS PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.MAVLinkPacket;
//import android.util.Log;

/**
* Message with some status from APM to GCS about camera or antenna mount
*/
public class msg_mount_status extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_MOUNT_STATUS = 158;
	public static final int MAVLINK_MSG_LENGTH = 14;
	private static final long serialVersionUID = MAVLINK_MSG_ID_MOUNT_STATUS;
	

 	/**
	* pitch(deg*100) or lat, depending on mount mode
	*/
	public int pointing_a; 
 	/**
	* roll(deg*100) or lon depending on mount mode
	*/
	public int pointing_b; 
 	/**
	* yaw(deg*100) or alt (in cm) depending on mount mode
	*/
	public int pointing_c; 
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
		packet.msgid = MAVLINK_MSG_ID_MOUNT_STATUS;
		packet.payload.putInt(pointing_a);
		packet.payload.putInt(pointing_b);
		packet.payload.putInt(pointing_c);
		packet.payload.putByte(target_system);
		packet.payload.putByte(target_component);
		return packet;		
	}

    /**
     * Decode a mount_status message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    pointing_a = payload.getInt();
	    pointing_b = payload.getInt();
	    pointing_c = payload.getInt();
	    target_system = payload.getByte();
	    target_component = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_mount_status(){
    	msgid = MAVLINK_MSG_ID_MOUNT_STATUS;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_mount_status(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_MOUNT_STATUS;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "MOUNT_STATUS");
        //Log.d("MAVLINK_MSG_ID_MOUNT_STATUS", toString());
    }
    
          
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_MOUNT_STATUS -"+" pointing_a:"+pointing_a+" pointing_b:"+pointing_b+" pointing_c:"+pointing_c+" target_system:"+target_system+" target_component:"+target_component+"";
    }
}
