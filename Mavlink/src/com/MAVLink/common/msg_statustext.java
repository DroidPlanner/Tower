        // MESSAGE STATUSTEXT PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Status text message. These messages are printed in yellow in the COMM console of QGroundControl. WARNING: They consume quite some bandwidth, so use only for important status and error messages. If implemented wisely, these messages are buffered on the MCU and sent only at a limited rate (e.g. 10 Hz).
        */
        public class msg_statustext extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_STATUSTEXT = 253;
        public static final int MAVLINK_MSG_LENGTH = 51;
        private static final long serialVersionUID = MAVLINK_MSG_ID_STATUSTEXT;
        
        
         	/**
        * Severity of status. Relies on the definitions within RFC-5424. See enum MAV_SEVERITY.
        */
        public byte severity;
         	/**
        * Status text message, without null termination character
        */
        public byte text[] = new byte[50];
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_STATUSTEXT;
        		packet.payload.putByte(severity);
        		 for (int i = 0; i < text.length; i++) {
                    packet.payload.putByte(text[i]);
                    }
        
		return packet;
        }
        
        /**
        * Decode a statustext message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.severity = payload.getByte();
        	     for (int i = 0; i < this.text.length; i++) {
                    this.text[i] = payload.getByte();
                    }
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_statustext(){
    	msgid = MAVLINK_MSG_ID_STATUSTEXT;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_statustext(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_STATUSTEXT;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "STATUSTEXT");
        //Log.d("MAVLINK_MSG_ID_STATUSTEXT", toString());
        }
        
           /**
                        * Sets the buffer of this message with a string, adds the necessary padding
                        */
                        public void setText(String str) {
                        int len = Math.min(str.length(), 50);
                        for (int i=0; i<len; i++) {
                        text[i] = (byte) str.charAt(i);
                        }
                        for (int i=len; i<50; i++) {			// padding for the rest of the buffer
                        text[i] = 0;
                        }
                        }
                        
                        /**
                        * Gets the message, formated as a string
                        */
                        public String getText() {
                        String result = "";
                        for (int i = 0; i < 50; i++) {
                        if (text[i] != 0)
                        result = result + (char) text[i];
                        else
                        break;
                        }
                        return result;
                        
                        } 
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_STATUSTEXT -"+" severity:"+severity+" text:"+text+"";
        }
        }
        