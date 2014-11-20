        // MESSAGE DATA_STREAM PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * 
        */
        public class msg_data_stream extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_DATA_STREAM = 67;
        public static final int MAVLINK_MSG_LENGTH = 4;
        private static final long serialVersionUID = MAVLINK_MSG_ID_DATA_STREAM;
        
        
         	/**
        * The requested interval between two messages of this type
        */
        public short message_rate;
         	/**
        * The ID of the requested data stream
        */
        public byte stream_id;
         	/**
        * 1 stream is enabled, 0 stream is stopped.
        */
        public byte on_off;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_DATA_STREAM;
        		packet.payload.putShort(message_rate);
        		packet.payload.putByte(stream_id);
        		packet.payload.putByte(on_off);
        
		return packet;
        }
        
        /**
        * Decode a data_stream message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.message_rate = payload.getShort();
        	    this.stream_id = payload.getByte();
        	    this.on_off = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_data_stream(){
    	msgid = MAVLINK_MSG_ID_DATA_STREAM;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_data_stream(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_DATA_STREAM;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "DATA_STREAM");
        //Log.d("MAVLINK_MSG_ID_DATA_STREAM", toString());
        }
        
              
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_DATA_STREAM -"+" message_rate:"+message_rate+" stream_id:"+stream_id+" on_off:"+on_off+"";
        }
        }
        