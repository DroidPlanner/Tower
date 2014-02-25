// MESSAGE FILE_TRANSFER_START PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Begin file transfer
*/
public class msg_file_transfer_start extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_FILE_TRANSFER_START = 110;
	public static final int MAVLINK_MSG_LENGTH = 254;
	private static final long serialVersionUID = MAVLINK_MSG_ID_FILE_TRANSFER_START;
	

 	/**
	* Unique transfer ID
	*/
	public long transfer_uid; 
 	/**
	* File size in bytes
	*/
	public int file_size; 
 	/**
	* Destination path
	*/
	public byte dest_path[] = new byte[240]; 
 	/**
	* Transfer direction: 0: from requester, 1: to requester
	*/
	public byte direction; 
 	/**
	* RESERVED
	*/
	public byte flags; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_FILE_TRANSFER_START;
		packet.payload.putLong(transfer_uid);
		packet.payload.putInt(file_size);
		 for (int i = 0; i < dest_path.length; i++) {
                        packet.payload.putByte(dest_path[i]);
            }
		packet.payload.putByte(direction);
		packet.payload.putByte(flags);
		return packet;		
	}

    /**
     * Decode a file_transfer_start message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    transfer_uid = payload.getLong();
	    file_size = payload.getInt();
	     for (int i = 0; i < dest_path.length; i++) {
			dest_path[i] = payload.getByte();
		}
	    direction = payload.getByte();
	    flags = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_file_transfer_start(){
    	msgid = MAVLINK_MSG_ID_FILE_TRANSFER_START;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_file_transfer_start(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_FILE_TRANSFER_START;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "FILE_TRANSFER_START");
        //Log.d("MAVLINK_MSG_ID_FILE_TRANSFER_START", toString());
    }
    
     /**
     * Sets the buffer of this message with a string, adds the necessary padding
     */    
    public void setDest_Path(String str) {
      int len = Math.min(str.length(), 240);
      for (int i=0; i<len; i++) {
        dest_path[i] = (byte) str.charAt(i);
      }
      for (int i=len; i<240; i++) {			// padding for the rest of the buffer
        dest_path[i] = 0;
      }
    }
    
    /**
	 * Gets the message, formated as a string
	 */
	public String getDest_Path() {
		String result = "";
		for (int i = 0; i < 240; i++) {
			if (dest_path[i] != 0)
				result = result + (char) dest_path[i];
			else
				break;
		}
		return result;
		
	}     
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_FILE_TRANSFER_START -"+" transfer_uid:"+transfer_uid+" file_size:"+file_size+" dest_path:"+dest_path+" direction:"+direction+" flags:"+flags+"";
    }
}
