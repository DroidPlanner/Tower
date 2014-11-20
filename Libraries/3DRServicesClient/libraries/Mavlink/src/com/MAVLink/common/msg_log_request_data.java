        // MESSAGE LOG_REQUEST_DATA PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Request a chunk of a log
        */
        public class msg_log_request_data extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_LOG_REQUEST_DATA = 119;
        public static final int MAVLINK_MSG_LENGTH = 12;
        private static final long serialVersionUID = MAVLINK_MSG_ID_LOG_REQUEST_DATA;
        
        
         	/**
        * Offset into the log
        */
        public int ofs;
         	/**
        * Number of bytes
        */
        public int count;
         	/**
        * Log id (from LOG_ENTRY reply)
        */
        public short id;
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
		packet.msgid = MAVLINK_MSG_ID_LOG_REQUEST_DATA;
        		packet.payload.putInt(ofs);
        		packet.payload.putInt(count);
        		packet.payload.putShort(id);
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        
		return packet;
        }
        
        /**
        * Decode a log_request_data message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.ofs = payload.getInt();
        	    this.count = payload.getInt();
        	    this.id = payload.getShort();
        	    this.target_system = payload.getByte();
        	    this.target_component = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_log_request_data(){
    	msgid = MAVLINK_MSG_ID_LOG_REQUEST_DATA;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_log_request_data(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_LOG_REQUEST_DATA;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "LOG_REQUEST_DATA");
        //Log.d("MAVLINK_MSG_ID_LOG_REQUEST_DATA", toString());
        }
        
                  
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_LOG_REQUEST_DATA -"+" ofs:"+ofs+" count:"+count+" id:"+id+" target_system:"+target_system+" target_component:"+target_component+"";
        }
        }
        