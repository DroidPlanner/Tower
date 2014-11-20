        // MESSAGE CHANGE_OPERATOR_CONTROL PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Request to control this MAV
        */
        public class msg_change_operator_control extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL = 5;
        public static final int MAVLINK_MSG_LENGTH = 28;
        private static final long serialVersionUID = MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL;
        
        
         	/**
        * System the GCS requests control for
        */
        public byte target_system;
         	/**
        * 0: request control of this MAV, 1: Release control of this MAV
        */
        public byte control_request;
         	/**
        * 0: key as plaintext, 1-255: future, different hashing/encryption variants. The GCS should in general use the safest mode possible initially and then gradually move down the encryption level if it gets a NACK message indicating an encryption mismatch.
        */
        public byte version;
         	/**
        * Password / Key, depending on version plaintext or encrypted. 25 or less characters, NULL terminated. The characters may involve A-Z, a-z, 0-9, and "!?,.-"
        */
        public byte passkey[] = new byte[25];
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL;
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(control_request);
        		packet.payload.putByte(version);
        		 for (int i = 0; i < passkey.length; i++) {
                    packet.payload.putByte(passkey[i]);
                    }
        
		return packet;
        }
        
        /**
        * Decode a change_operator_control message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.target_system = payload.getByte();
        	    this.control_request = payload.getByte();
        	    this.version = payload.getByte();
        	     for (int i = 0; i < this.passkey.length; i++) {
                    this.passkey[i] = payload.getByte();
                    }
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_change_operator_control(){
    	msgid = MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_change_operator_control(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "CHANGE_OPERATOR_CONTROL");
        //Log.d("MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL", toString());
        }
        
               /**
                        * Sets the buffer of this message with a string, adds the necessary padding
                        */
                        public void setPasskey(String str) {
                        int len = Math.min(str.length(), 25);
                        for (int i=0; i<len; i++) {
                        passkey[i] = (byte) str.charAt(i);
                        }
                        for (int i=len; i<25; i++) {			// padding for the rest of the buffer
                        passkey[i] = 0;
                        }
                        }
                        
                        /**
                        * Gets the message, formated as a string
                        */
                        public String getPasskey() {
                        String result = "";
                        for (int i = 0; i < 25; i++) {
                        if (passkey[i] != 0)
                        result = result + (char) passkey[i];
                        else
                        break;
                        }
                        return result;
                        
                        } 
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL -"+" target_system:"+target_system+" control_request:"+control_request+" version:"+version+" passkey:"+passkey+"";
        }
        }
        