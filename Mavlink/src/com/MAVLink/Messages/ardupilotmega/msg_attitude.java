// MESSAGE ATTITUDE PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.MAVLinkPacket;
//import android.util.Log;

/**
* The attitude in the aeronautical frame (right-handed, Z-down, X-front, Y-right).
*/
public class msg_attitude extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_ATTITUDE = 30;
	public static final int MAVLINK_MSG_LENGTH = 28;
	private static final long serialVersionUID = MAVLINK_MSG_ID_ATTITUDE;
	

 	/**
	* Timestamp (milliseconds since system boot)
	*/
	public int time_boot_ms; 
 	/**
	* Roll angle (rad, -pi..+pi)
	*/
	public float roll; 
 	/**
	* Pitch angle (rad, -pi..+pi)
	*/
	public float pitch; 
 	/**
	* Yaw angle (rad, -pi..+pi)
	*/
	public float yaw; 
 	/**
	* Roll angular speed (rad/s)
	*/
	public float rollspeed; 
 	/**
	* Pitch angular speed (rad/s)
	*/
	public float pitchspeed; 
 	/**
	* Yaw angular speed (rad/s)
	*/
	public float yawspeed; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_ATTITUDE;
		packet.payload.putInt(time_boot_ms);
		packet.payload.putFloat(roll);
		packet.payload.putFloat(pitch);
		packet.payload.putFloat(yaw);
		packet.payload.putFloat(rollspeed);
		packet.payload.putFloat(pitchspeed);
		packet.payload.putFloat(yawspeed);
		return packet;		
	}

    /**
     * Decode a attitude message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    time_boot_ms = payload.getInt();
	    roll = payload.getFloat();
	    pitch = payload.getFloat();
	    yaw = payload.getFloat();
	    rollspeed = payload.getFloat();
	    pitchspeed = payload.getFloat();
	    yawspeed = payload.getFloat();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_attitude(){
    	msgid = MAVLINK_MSG_ID_ATTITUDE;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_attitude(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_ATTITUDE;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "ATTITUDE");
        //Log.d("MAVLINK_MSG_ID_ATTITUDE", toString());
    }
    
              
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_ATTITUDE -"+" time_boot_ms:"+time_boot_ms+" roll:"+roll+" pitch:"+pitch+" yaw:"+yaw+" rollspeed:"+rollspeed+" pitchspeed:"+pitchspeed+" yawspeed:"+yawspeed+"";
    }
}
