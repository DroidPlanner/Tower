// MESSAGE SET_MAG_OFFSETS PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.MAVLinkPacket;
//import android.util.Log;

/**
* set the magnetometer offsets
*/
public class msg_set_mag_offsets extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_SET_MAG_OFFSETS = 151;
	public static final int MAVLINK_MSG_LENGTH = 8;
	private static final long serialVersionUID = MAVLINK_MSG_ID_SET_MAG_OFFSETS;
	

 	/**
	* magnetometer X offset
	*/
	public short mag_ofs_x; 
 	/**
	* magnetometer Y offset
	*/
	public short mag_ofs_y; 
 	/**
	* magnetometer Z offset
	*/
	public short mag_ofs_z; 
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
		packet.msgid = MAVLINK_MSG_ID_SET_MAG_OFFSETS;
		packet.payload.putShort(mag_ofs_x);
		packet.payload.putShort(mag_ofs_y);
		packet.payload.putShort(mag_ofs_z);
		packet.payload.putByte(target_system);
		packet.payload.putByte(target_component);
		return packet;		
	}

    /**
     * Decode a set_mag_offsets message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    mag_ofs_x = payload.getShort();
	    mag_ofs_y = payload.getShort();
	    mag_ofs_z = payload.getShort();
	    target_system = payload.getByte();
	    target_component = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_set_mag_offsets(){
    	msgid = MAVLINK_MSG_ID_SET_MAG_OFFSETS;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_set_mag_offsets(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SET_MAG_OFFSETS;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "SET_MAG_OFFSETS");
        //Log.d("MAVLINK_MSG_ID_SET_MAG_OFFSETS", toString());
    }
    
          
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_SET_MAG_OFFSETS -"+" mag_ofs_x:"+mag_ofs_x+" mag_ofs_y:"+mag_ofs_y+" mag_ofs_z:"+mag_ofs_z+" target_system:"+target_system+" target_component:"+target_component+"";
    }
}
