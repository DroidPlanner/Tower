// MESSAGE HIL_STATE PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* DEPRECATED PACKET! Suffers from missing airspeed fields and singularities due to Euler angles. Please use HIL_STATE_QUATERNION instead. Sent from simulation to autopilot. This packet is useful for high throughput applications such as hardware in the loop simulations.
*/
public class msg_hil_state extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_HIL_STATE = 90;
	public static final int MAVLINK_MSG_LENGTH = 56;
	private static final long serialVersionUID = MAVLINK_MSG_ID_HIL_STATE;
	

 	/**
	* Timestamp (microseconds since UNIX epoch or microseconds since system boot)
	*/
	public long time_usec; 
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
	* Body frame roll / phi angular speed (rad/s)
	*/
	public float rollspeed; 
 	/**
	* Body frame pitch / theta angular speed (rad/s)
	*/
	public float pitchspeed; 
 	/**
	* Body frame yaw / psi angular speed (rad/s)
	*/
	public float yawspeed; 
 	/**
	* Latitude, expressed as * 1E7
	*/
	public int lat; 
 	/**
	* Longitude, expressed as * 1E7
	*/
	public int lon; 
 	/**
	* Altitude in meters, expressed as * 1000 (millimeters)
	*/
	public int alt; 
 	/**
	* Ground X Speed (Latitude), expressed as m/s * 100
	*/
	public short vx; 
 	/**
	* Ground Y Speed (Longitude), expressed as m/s * 100
	*/
	public short vy; 
 	/**
	* Ground Z Speed (Altitude), expressed as m/s * 100
	*/
	public short vz; 
 	/**
	* X acceleration (mg)
	*/
	public short xacc; 
 	/**
	* Y acceleration (mg)
	*/
	public short yacc; 
 	/**
	* Z acceleration (mg)
	*/
	public short zacc; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_HIL_STATE;
		packet.payload.putLong(time_usec);
		packet.payload.putFloat(roll);
		packet.payload.putFloat(pitch);
		packet.payload.putFloat(yaw);
		packet.payload.putFloat(rollspeed);
		packet.payload.putFloat(pitchspeed);
		packet.payload.putFloat(yawspeed);
		packet.payload.putInt(lat);
		packet.payload.putInt(lon);
		packet.payload.putInt(alt);
		packet.payload.putShort(vx);
		packet.payload.putShort(vy);
		packet.payload.putShort(vz);
		packet.payload.putShort(xacc);
		packet.payload.putShort(yacc);
		packet.payload.putShort(zacc);
		return packet;		
	}

    /**
     * Decode a hil_state message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    time_usec = payload.getLong();
	    roll = payload.getFloat();
	    pitch = payload.getFloat();
	    yaw = payload.getFloat();
	    rollspeed = payload.getFloat();
	    pitchspeed = payload.getFloat();
	    yawspeed = payload.getFloat();
	    lat = payload.getInt();
	    lon = payload.getInt();
	    alt = payload.getInt();
	    vx = payload.getShort();
	    vy = payload.getShort();
	    vz = payload.getShort();
	    xacc = payload.getShort();
	    yacc = payload.getShort();
	    zacc = payload.getShort();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_hil_state(){
    	msgid = MAVLINK_MSG_ID_HIL_STATE;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_hil_state(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_HIL_STATE;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "HIL_STATE");
        //Log.d("MAVLINK_MSG_ID_HIL_STATE", toString());
    }
    
                                
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_HIL_STATE -"+" time_usec:"+time_usec+" roll:"+roll+" pitch:"+pitch+" yaw:"+yaw+" rollspeed:"+rollspeed+" pitchspeed:"+pitchspeed+" yawspeed:"+yawspeed+" lat:"+lat+" lon:"+lon+" alt:"+alt+" vx:"+vx+" vy:"+vy+" vz:"+vz+" xacc:"+xacc+" yacc:"+yacc+" zacc:"+zacc+"";
    }
}
