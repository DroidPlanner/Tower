        // MESSAGE AUTOPILOT_VERSION PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Version and capability of autopilot software
        */
        public class msg_autopilot_version extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_AUTOPILOT_VERSION = 148;
        public static final int MAVLINK_MSG_LENGTH = 20;
        private static final long serialVersionUID = MAVLINK_MSG_ID_AUTOPILOT_VERSION;
        
        
         	/**
        * bitmask of capabilities (see MAV_PROTOCOL_CAPABILITY enum)
        */
        public long capabilities;
         	/**
        * Firmware version number
        */
        public int version;
         	/**
        * Custom version field, commonly the first 8 bytes (16 characters) of the git hash. This is not an unique identifier, but should allow to identify the commit using the main version number even for very large code bases.
        */
        public byte custom_version[] = new byte[8];
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_AUTOPILOT_VERSION;
        		packet.payload.putLong(capabilities);
        		packet.payload.putInt(version);
        		 for (int i = 0; i < custom_version.length; i++) {
                    packet.payload.putByte(custom_version[i]);
                    }
        
		return packet;
        }
        
        /**
        * Decode a autopilot_version message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.capabilities = payload.getLong();
        	    this.version = payload.getInt();
        	     for (int i = 0; i < this.custom_version.length; i++) {
                    this.custom_version[i] = payload.getByte();
                    }
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_autopilot_version(){
    	msgid = MAVLINK_MSG_ID_AUTOPILOT_VERSION;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_autopilot_version(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_AUTOPILOT_VERSION;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "AUTOPILOT_VERSION");
        //Log.d("MAVLINK_MSG_ID_AUTOPILOT_VERSION", toString());
        }
        
              
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_AUTOPILOT_VERSION -"+" capabilities:"+capabilities+" version:"+version+" custom_version:"+custom_version+"";
        }
        }
        