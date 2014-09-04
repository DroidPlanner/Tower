// MESSAGE DATA_TRANSMISSION_HANDSHAKE PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* 
*/
public class msg_data_transmission_handshake extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_DATA_TRANSMISSION_HANDSHAKE = 130;
	public static final int MAVLINK_MSG_LENGTH = 13;
	private static final long serialVersionUID = MAVLINK_MSG_ID_DATA_TRANSMISSION_HANDSHAKE;
	

 	/**
	* total data size in bytes (set on ACK only)
	*/
	public int size; 
 	/**
	* Width of a matrix or image
	*/
	public short width; 
 	/**
	* Height of a matrix or image
	*/
	public short height; 
 	/**
	* number of packets beeing sent (set on ACK only)
	*/
	public short packets; 
 	/**
	* type of requested/acknowledged data (as defined in ENUM DATA_TYPES in mavlink/include/mavlink_types.h)
	*/
	public byte type; 
 	/**
	* payload size per packet (normally 253 byte, see DATA field size in message ENCAPSULATED_DATA) (set on ACK only)
	*/
	public byte payload; 
 	/**
	* JPEG quality out of [1,100]
	*/
	public byte jpg_quality; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_DATA_TRANSMISSION_HANDSHAKE;
		packet.payload.putInt(size);
		packet.payload.putShort(width);
		packet.payload.putShort(height);
		packet.payload.putShort(packets);
		packet.payload.putByte(type);
		packet.payload.putByte(payload);
		packet.payload.putByte(jpg_quality);
		return packet;		
	}

    /**
     * Decode a data_transmission_handshake message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    size = payload.getInt();
	    width = payload.getShort();
	    height = payload.getShort();
	    packets = payload.getShort();
	    type = payload.getByte();
	    //payload = payload.getByte(); TODO fix this message
	    jpg_quality = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_data_transmission_handshake(){
    	msgid = MAVLINK_MSG_ID_DATA_TRANSMISSION_HANDSHAKE;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_data_transmission_handshake(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_DATA_TRANSMISSION_HANDSHAKE;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "DATA_TRANSMISSION_HANDSHAKE");
        //Log.d("MAVLINK_MSG_ID_DATA_TRANSMISSION_HANDSHAKE", toString());
    }
    
              
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_DATA_TRANSMISSION_HANDSHAKE -"+" size:"+size+" width:"+width+" height:"+height+" packets:"+packets+" type:"+type+" payload:"+payload+" jpg_quality:"+jpg_quality+"";
    }
}
