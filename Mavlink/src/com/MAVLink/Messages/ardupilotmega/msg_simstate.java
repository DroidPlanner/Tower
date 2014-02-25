// MESSAGE SIMSTATE PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Status of simulation environment, if used
*/
public class msg_simstate extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_SIMSTATE = 164;
	public static final int MAVLINK_MSG_LENGTH = 44;
	private static final long serialVersionUID = MAVLINK_MSG_ID_SIMSTATE;
	

 	/**
	* Roll angle (rad)
	*/
	public float roll; 
 	/**
	* Pitch angle (rad)
	*/
	public float pitch; 
 	/**
	* Yaw angle (rad)
	*/
	public float yaw; 
 	/**
	* X acceleration m/s/s
	*/
	public float xacc; 
 	/**
	* Y acceleration m/s/s
	*/
	public float yacc; 
 	/**
	* Z acceleration m/s/s
	*/
	public float zacc; 
 	/**
	* Angular speed around X axis rad/s
	*/
	public float xgyro; 
 	/**
	* Angular speed around Y axis rad/s
	*/
	public float ygyro; 
 	/**
	* Angular speed around Z axis rad/s
	*/
	public float zgyro; 
 	/**
	* Latitude in degrees
	*/
	public float lat; 
 	/**
	* Longitude in degrees
	*/
	public float lng; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_SIMSTATE;
		packet.payload.putFloat(roll);
		packet.payload.putFloat(pitch);
		packet.payload.putFloat(yaw);
		packet.payload.putFloat(xacc);
		packet.payload.putFloat(yacc);
		packet.payload.putFloat(zacc);
		packet.payload.putFloat(xgyro);
		packet.payload.putFloat(ygyro);
		packet.payload.putFloat(zgyro);
		packet.payload.putFloat(lat);
		packet.payload.putFloat(lng);
		return packet;		
	}

    /**
     * Decode a simstate message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    roll = payload.getFloat();
	    pitch = payload.getFloat();
	    yaw = payload.getFloat();
	    xacc = payload.getFloat();
	    yacc = payload.getFloat();
	    zacc = payload.getFloat();
	    xgyro = payload.getFloat();
	    ygyro = payload.getFloat();
	    zgyro = payload.getFloat();
	    lat = payload.getFloat();
	    lng = payload.getFloat();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_simstate(){
    	msgid = MAVLINK_MSG_ID_SIMSTATE;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_simstate(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SIMSTATE;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "SIMSTATE");
        //Log.d("MAVLINK_MSG_ID_SIMSTATE", toString());
    }
    
                      
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_SIMSTATE -"+" roll:"+roll+" pitch:"+pitch+" yaw:"+yaw+" xacc:"+xacc+" yacc:"+yacc+" zacc:"+zacc+" xgyro:"+xgyro+" ygyro:"+ygyro+" zgyro:"+zgyro+" lat:"+lat+" lng:"+lng+"";
    }
}
