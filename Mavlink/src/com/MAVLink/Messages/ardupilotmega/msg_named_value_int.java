// MESSAGE NAMED_VALUE_INT PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Send a key-value pair as integer. The use of this message is discouraged for normal packets, but a quite efficient way for testing new messages and getting experimental debug output.
*/
public class msg_named_value_int extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_NAMED_VALUE_INT = 252;
	public static final int MAVLINK_MSG_LENGTH = 18;
	private static final long serialVersionUID = MAVLINK_MSG_ID_NAMED_VALUE_INT;
	

 	/**
	* Timestamp (milliseconds since system boot)
	*/
	public int time_boot_ms; 
 	/**
	* Signed integer value
	*/
	public int value; 
 	/**
	* Name of the debug variable
	*/
	public byte name[] = new byte[10]; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_NAMED_VALUE_INT;
		packet.payload.putInt(time_boot_ms);
		packet.payload.putInt(value);
		 for (int i = 0; i < name.length; i++) {
                        packet.payload.putByte(name[i]);
            }
		return packet;		
	}

    /**
     * Decode a named_value_int message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    time_boot_ms = payload.getInt();
	    value = payload.getInt();
	     for (int i = 0; i < name.length; i++) {
			name[i] = payload.getByte();
		}    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_named_value_int(){
    	msgid = MAVLINK_MSG_ID_NAMED_VALUE_INT;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_named_value_int(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_NAMED_VALUE_INT;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "NAMED_VALUE_INT");
        //Log.d("MAVLINK_MSG_ID_NAMED_VALUE_INT", toString());
    }
    
     /**
     * Sets the buffer of this message with a string, adds the necessary padding
     */    
    public void setName(String str) {
      int len = Math.min(str.length(), 10);
      for (int i=0; i<len; i++) {
        name[i] = (byte) str.charAt(i);
      }
      for (int i=len; i<10; i++) {			// padding for the rest of the buffer
        name[i] = 0;
      }
    }
    
    /**
	 * Gets the message, formated as a string
	 */
	public String getName() {
		String result = "";
		for (int i = 0; i < 10; i++) {
			if (name[i] != 0)
				result = result + (char) name[i];
			else
				break;
		}
		return result;
		
	} 
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_NAMED_VALUE_INT -"+" time_boot_ms:"+time_boot_ms+" value:"+value+" name:"+name+"";
    }
}
