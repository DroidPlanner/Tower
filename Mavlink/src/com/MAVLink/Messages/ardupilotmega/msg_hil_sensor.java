// MESSAGE HIL_SENSOR PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* The IMU readings in SI units in NED body frame
*/
public class msg_hil_sensor extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_HIL_SENSOR = 107;
	public static final int MAVLINK_MSG_LENGTH = 64;
	private static final long serialVersionUID = MAVLINK_MSG_ID_HIL_SENSOR;
	

 	/**
	* Timestamp (microseconds, synced to UNIX time or since system boot)
	*/
	public long time_usec; 
 	/**
	* X acceleration (m/s^2)
	*/
	public float xacc; 
 	/**
	* Y acceleration (m/s^2)
	*/
	public float yacc; 
 	/**
	* Z acceleration (m/s^2)
	*/
	public float zacc; 
 	/**
	* Angular speed around X axis in body frame (rad / sec)
	*/
	public float xgyro; 
 	/**
	* Angular speed around Y axis in body frame (rad / sec)
	*/
	public float ygyro; 
 	/**
	* Angular speed around Z axis in body frame (rad / sec)
	*/
	public float zgyro; 
 	/**
	* X Magnetic field (Gauss)
	*/
	public float xmag; 
 	/**
	* Y Magnetic field (Gauss)
	*/
	public float ymag; 
 	/**
	* Z Magnetic field (Gauss)
	*/
	public float zmag; 
 	/**
	* Absolute pressure in millibar
	*/
	public float abs_pressure; 
 	/**
	* Differential pressure (airspeed) in millibar
	*/
	public float diff_pressure; 
 	/**
	* Altitude calculated from pressure
	*/
	public float pressure_alt; 
 	/**
	* Temperature in degrees celsius
	*/
	public float temperature; 
 	/**
	* Bitmask for fields that have updated since last message, bit 0 = xacc, bit 12: temperature
	*/
	public int fields_updated; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_HIL_SENSOR;
		packet.payload.putLong(time_usec);
		packet.payload.putFloat(xacc);
		packet.payload.putFloat(yacc);
		packet.payload.putFloat(zacc);
		packet.payload.putFloat(xgyro);
		packet.payload.putFloat(ygyro);
		packet.payload.putFloat(zgyro);
		packet.payload.putFloat(xmag);
		packet.payload.putFloat(ymag);
		packet.payload.putFloat(zmag);
		packet.payload.putFloat(abs_pressure);
		packet.payload.putFloat(diff_pressure);
		packet.payload.putFloat(pressure_alt);
		packet.payload.putFloat(temperature);
		packet.payload.putInt(fields_updated);
		return packet;		
	}

    /**
     * Decode a hil_sensor message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    time_usec = payload.getLong();
	    xacc = payload.getFloat();
	    yacc = payload.getFloat();
	    zacc = payload.getFloat();
	    xgyro = payload.getFloat();
	    ygyro = payload.getFloat();
	    zgyro = payload.getFloat();
	    xmag = payload.getFloat();
	    ymag = payload.getFloat();
	    zmag = payload.getFloat();
	    abs_pressure = payload.getFloat();
	    diff_pressure = payload.getFloat();
	    pressure_alt = payload.getFloat();
	    temperature = payload.getFloat();
	    fields_updated = payload.getInt();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_hil_sensor(){
    	msgid = MAVLINK_MSG_ID_HIL_SENSOR;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_hil_sensor(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_HIL_SENSOR;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "HIL_SENSOR");
        //Log.d("MAVLINK_MSG_ID_HIL_SENSOR", toString());
    }
    
                              
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_HIL_SENSOR -"+" time_usec:"+time_usec+" xacc:"+xacc+" yacc:"+yacc+" zacc:"+zacc+" xgyro:"+xgyro+" ygyro:"+ygyro+" zgyro:"+zgyro+" xmag:"+xmag+" ymag:"+ymag+" zmag:"+zmag+" abs_pressure:"+abs_pressure+" diff_pressure:"+diff_pressure+" pressure_alt:"+pressure_alt+" temperature:"+temperature+" fields_updated:"+fields_updated+"";
    }
}
