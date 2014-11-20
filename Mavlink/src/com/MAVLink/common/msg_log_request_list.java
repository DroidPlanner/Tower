        // MESSAGE LOG_REQUEST_LIST PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Request a list of available logs. On some systems calling this may stop on-board logging until LOG_REQUEST_END is called.
        */
        public class msg_log_request_list extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_LOG_REQUEST_LIST = 117;
        public static final int MAVLINK_MSG_LENGTH = 6;
        private static final long serialVersionUID = MAVLINK_MSG_ID_LOG_REQUEST_LIST;
        
        
         	/**
        * First log id (0 for first available)
        */
        public short start;
         	/**
        * Last log id (0xffff for last available)
        */
        public short end;
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
		packet.msgid = MAVLINK_MSG_ID_LOG_REQUEST_LIST;
        		packet.payload.putShort(start);
        		packet.payload.putShort(end);
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        
		return packet;
        }
        
        /**
        * Decode a log_request_list message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.start = payload.getShort();
        	    this.end = payload.getShort();
        	    this.target_system = payload.getByte();
        	    this.target_component = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_log_request_list(){
    	msgid = MAVLINK_MSG_ID_LOG_REQUEST_LIST;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_log_request_list(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_LOG_REQUEST_LIST;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "LOG_REQUEST_LIST");
        //Log.d("MAVLINK_MSG_ID_LOG_REQUEST_LIST", toString());
        }
        
                
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_LOG_REQUEST_LIST -"+" start:"+start+" end:"+end+" target_system:"+target_system+" target_component:"+target_component+"";
        }
        }
        