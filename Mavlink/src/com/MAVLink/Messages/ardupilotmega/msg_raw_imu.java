// MESSAGE RAW_IMU PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* The RAW IMU readings for the usual 9DOF sensor setup. This message should always contain the true raw values without any scaling to allow data capture and system debugging.
*/
public class msg_raw_imu extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_RAW_IMU = 27;
	public static final int MAVLINK_MSG_LENGTH = 26;
	private static final long serialVersionUID = MAVLINK_MSG_ID_RAW_IMU;
	

 	/**
	* Timestamp (microseconds since UNIX epoch or microseconds since system boot)
	*/
	public long time_usec; 
 	/**
	* X acceleration (raw)
	*/
	public short xacc; 
 	/**
	* Y acceleration (raw)
	*/
	public short yacc; 
 	/**
	* Z acceleration (raw)
	*/
	public short zacc; 
 	/**
	* Angular speed around X axis (raw)
	*/
	public short xgyro; 
 	/**
	* Angular speed around Y axis (raw)
	*/
	public short ygyro; 
 	/**
	* Angular speed around Z axis (raw)
	*/
	public short zgyro; 
 	/**
	* X Magnetic field (raw)
	*/
	public short xmag; 
 	/**
	* Y Magnetic field (raw)
	*/
	public short ymag; 
 	/**
	* Z Magnetic field (raw)
	*/
	public short zmag; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_RAW_IMU;
		packet.payload.putLong(time_usec);
		packet.payload.putShort(xacc);
		packet.payload.putShort(yacc);
		packet.payload.putShort(zacc);
		packet.payload.putShort(xgyro);
		packet.payload.putShort(ygyro);
		packet.payload.putShort(zgyro);
		packet.payload.putShort(xmag);
		packet.payload.putShort(ymag);
		packet.payload.putShort(zmag);
		return packet;		
	}

    /**
     * Decode a raw_imu message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    time_usec = payload.getLong();
	    xacc = payload.getShort();
	    yacc = payload.getShort();
	    zacc = payload.getShort();
	    xgyro = payload.getShort();
	    ygyro = payload.getShort();
	    zgyro = payload.getShort();
	    xmag = payload.getShort();
	    ymag = payload.getShort();
	    zmag = payload.getShort();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_raw_imu(){
    	msgid = MAVLINK_MSG_ID_RAW_IMU;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_raw_imu(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_RAW_IMU;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "RAW_IMU");
        //Log.d("MAVLINK_MSG_ID_RAW_IMU", toString());
    }
    
                    
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_RAW_IMU -"+" time_usec:"+time_usec+" xacc:"+xacc+" yacc:"+yacc+" zacc:"+zacc+" xgyro:"+xgyro+" ygyro:"+ygyro+" zgyro:"+zgyro+" xmag:"+xmag+" ymag:"+ymag+" zmag:"+zmag+"";
    }
}
