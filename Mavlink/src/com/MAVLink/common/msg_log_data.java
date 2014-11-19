        // MESSAGE LOG_DATA PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Reply to LOG_REQUEST_DATA
        */
        public class msg_log_data extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_LOG_DATA = 120;
        public static final int MAVLINK_MSG_LENGTH = 97;
        private static final long serialVersionUID = MAVLINK_MSG_ID_LOG_DATA;
        
        
         	/**
        * Offset into the log
        */
        public int ofs;
         	/**
        * Log id (from LOG_ENTRY reply)
        */
        public short id;
         	/**
        * Number of bytes (zero for end of log)
        */
        public byte count;
         	/**
        * log data
        */
        public byte data[] = new byte[90];
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_LOG_DATA;
        		packet.payload.putInt(ofs);
        		packet.payload.putShort(id);
        		packet.payload.putByte(count);
        		 for (int i = 0; i < data.length; i++) {
                    packet.payload.putByte(data[i]);
                    }
        
		return packet;
        }
        
        /**
        * Decode a log_data message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.ofs = payload.getInt();
        	    this.id = payload.getShort();
        	    this.count = payload.getByte();
        	     for (int i = 0; i < this.data.length; i++) {
                    this.data[i] = payload.getByte();
                    }
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_log_data(){
    	msgid = MAVLINK_MSG_ID_LOG_DATA;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_log_data(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_LOG_DATA;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "LOG_DATA");
        //Log.d("MAVLINK_MSG_ID_LOG_DATA", toString());
        }
        
                
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_LOG_DATA -"+" ofs:"+ofs+" id:"+id+" count:"+count+" data:"+data+"";
        }
        }
        