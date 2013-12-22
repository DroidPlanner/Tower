// MESSAGE DATA64 PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.MAVLinkPacket;
//import android.util.Log;

/**
* Data packet, size 64
*/
public class msg_data64 extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_DATA64 = 171;
	public static final int MAVLINK_MSG_LENGTH = 66;
	private static final long serialVersionUID = MAVLINK_MSG_ID_DATA64;
	

 	/**
	* data type
	*/
	public byte type; 
 	/**
	* data length
	*/
	public byte len; 
 	/**
	* raw data
	*/
	public byte data[] = new byte[64]; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_DATA64;
		packet.payload.putByte(type);
		packet.payload.putByte(len);
		 for (int i = 0; i < data.length; i++) {
                        packet.payload.putByte(data[i]);
            }
		return packet;		
	}

    /**
     * Decode a data64 message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    type = payload.getByte();
	    len = payload.getByte();
	     for (int i = 0; i < data.length; i++) {
			data[i] = payload.getByte();
		}    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_data64(){
    	msgid = MAVLINK_MSG_ID_DATA64;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_data64(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_DATA64;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "DATA64");
        //Log.d("MAVLINK_MSG_ID_DATA64", toString());
    }
    
      
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_DATA64 -"+" type:"+type+" len:"+len+" data:"+data+"";
    }
}
