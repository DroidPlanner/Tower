// MESSAGE ATTITUDE_QUATERNION PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.MAVLinkPacket;
//import android.util.Log;

/**
* The attitude in the aeronautical frame (right-handed, Z-down, X-front, Y-right), expressed as quaternion.
*/
public class msg_attitude_quaternion extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_ATTITUDE_QUATERNION = 31;
	public static final int MAVLINK_MSG_LENGTH = 32;
	private static final long serialVersionUID = MAVLINK_MSG_ID_ATTITUDE_QUATERNION;
	

 	/**
	* Timestamp (milliseconds since system boot)
	*/
	public int time_boot_ms; 
 	/**
	* Quaternion component 1
	*/
	public float q1; 
 	/**
	* Quaternion component 2
	*/
	public float q2; 
 	/**
	* Quaternion component 3
	*/
	public float q3; 
 	/**
	* Quaternion component 4
	*/
	public float q4; 
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
		packet.msgid = MAVLINK_MSG_ID_ATTITUDE_QUATERNION;
		packet.payload.putInt(time_boot_ms);
		packet.payload.putFloat(q1);
		packet.payload.putFloat(q2);
		packet.payload.putFloat(q3);
		packet.payload.putFloat(q4);
		packet.payload.putFloat(rollspeed);
		packet.payload.putFloat(pitchspeed);
		packet.payload.putFloat(yawspeed);
		return packet;		
	}

    /**
     * Decode a attitude_quaternion message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    time_boot_ms = payload.getInt();
	    q1 = payload.getFloat();
	    q2 = payload.getFloat();
	    q3 = payload.getFloat();
	    q4 = payload.getFloat();
	    rollspeed = payload.getFloat();
	    pitchspeed = payload.getFloat();
	    yawspeed = payload.getFloat();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_attitude_quaternion(){
    	msgid = MAVLINK_MSG_ID_ATTITUDE_QUATERNION;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_attitude_quaternion(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_ATTITUDE_QUATERNION;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "ATTITUDE_QUATERNION");
        //Log.d("MAVLINK_MSG_ID_ATTITUDE_QUATERNION", toString());
    }
    
                
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_ATTITUDE_QUATERNION -"+" time_boot_ms:"+time_boot_ms+" q1:"+q1+" q2:"+q2+" q3:"+q3+" q4:"+q4+" rollspeed:"+rollspeed+" pitchspeed:"+pitchspeed+" yawspeed:"+yawspeed+"";
    }
}
