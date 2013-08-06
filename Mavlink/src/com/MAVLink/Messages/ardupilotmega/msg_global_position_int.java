// MESSAGE GLOBAL_POSITION_INT PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.MAVLinkPacket;
//import android.util.Log;

/**
* The filtered global position (e.g. fused GPS and accelerometers). The position is in GPS-frame (right-handed, Z-up). It
               is designed as scaled integer message since the resolution of float is not sufficient.
*/
public class msg_global_position_int extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_GLOBAL_POSITION_INT = 33;
	public static final int MAVLINK_MSG_LENGTH = 28;
	private static final long serialVersionUID = MAVLINK_MSG_ID_GLOBAL_POSITION_INT;
	

 	/**
	* Timestamp (milliseconds since system boot)
	*/
	public int time_boot_ms; 
 	/**
	* Latitude, expressed as * 1E7
	*/
	public int lat; 
 	/**
	* Longitude, expressed as * 1E7
	*/
	public int lon; 
 	/**
	* Altitude in meters, expressed as * 1000 (millimeters), above MSL
	*/
	public int alt; 
 	/**
	* Altitude above ground in meters, expressed as * 1000 (millimeters)
	*/
	public int relative_alt; 
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
	* Compass heading in degrees * 100, 0.0..359.99 degrees. If unknown, set to: 65535
	*/
	public short hdg; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_GLOBAL_POSITION_INT;
		packet.payload.putInt(time_boot_ms);
		packet.payload.putInt(lat);
		packet.payload.putInt(lon);
		packet.payload.putInt(alt);
		packet.payload.putInt(relative_alt);
		packet.payload.putShort(vx);
		packet.payload.putShort(vy);
		packet.payload.putShort(vz);
		packet.payload.putShort(hdg);
		return packet;		
	}

    /**
     * Decode a global_position_int message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    time_boot_ms = payload.getInt();
	    lat = payload.getInt();
	    lon = payload.getInt();
	    alt = payload.getInt();
	    relative_alt = payload.getInt();
	    vx = payload.getShort();
	    vy = payload.getShort();
	    vz = payload.getShort();
	    hdg = payload.getShort();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_global_position_int(){
    	msgid = MAVLINK_MSG_ID_GLOBAL_POSITION_INT;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_global_position_int(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GLOBAL_POSITION_INT;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "GLOBAL_POSITION_INT");
        //Log.d("MAVLINK_MSG_ID_GLOBAL_POSITION_INT", toString());
    }
    
                  
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_GLOBAL_POSITION_INT -"+" time_boot_ms:"+time_boot_ms+" lat:"+lat+" lon:"+lon+" alt:"+alt+" relative_alt:"+relative_alt+" vx:"+vx+" vy:"+vy+" vz:"+vz+" hdg:"+hdg+"";
    }
}
