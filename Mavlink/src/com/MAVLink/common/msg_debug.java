        // MESSAGE DEBUG PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Send a debug value. The index is used to discriminate between values. These values show up in the plot of QGroundControl as DEBUG N.
        */
        public class msg_debug extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_DEBUG = 254;
        public static final int MAVLINK_MSG_LENGTH = 9;
        private static final long serialVersionUID = MAVLINK_MSG_ID_DEBUG;
        
        
         	/**
        * Timestamp (milliseconds since system boot)
        */
        public int time_boot_ms;
         	/**
        * DEBUG value
        */
        public float value;
         	/**
        * index of debug variable
        */
        public byte ind;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_DEBUG;
        		packet.payload.putInt(time_boot_ms);
        		packet.payload.putFloat(value);
        		packet.payload.putByte(ind);
        
		return packet;
        }
        
        /**
        * Decode a debug message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_boot_ms = payload.getInt();
        	    this.value = payload.getFloat();
        	    this.ind = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_debug(){
    	msgid = MAVLINK_MSG_ID_DEBUG;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_debug(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_DEBUG;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "DEBUG");
        //Log.d("MAVLINK_MSG_ID_DEBUG", toString());
        }
        
              
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_DEBUG -"+" time_boot_ms:"+time_boot_ms+" value:"+value+" ind:"+ind+"";
        }
        }
        