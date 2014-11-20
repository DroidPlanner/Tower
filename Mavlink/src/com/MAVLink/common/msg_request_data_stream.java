        // MESSAGE REQUEST_DATA_STREAM PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * 
        */
        public class msg_request_data_stream extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_REQUEST_DATA_STREAM = 66;
        public static final int MAVLINK_MSG_LENGTH = 6;
        private static final long serialVersionUID = MAVLINK_MSG_ID_REQUEST_DATA_STREAM;
        
        
         	/**
        * The requested interval between two messages of this type
        */
        public short req_message_rate;
         	/**
        * The target requested to send the message stream.
        */
        public byte target_system;
         	/**
        * The target requested to send the message stream.
        */
        public byte target_component;
         	/**
        * The ID of the requested data stream
        */
        public byte req_stream_id;
         	/**
        * 1 to start sending, 0 to stop sending.
        */
        public byte start_stop;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_REQUEST_DATA_STREAM;
        		packet.payload.putShort(req_message_rate);
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        		packet.payload.putByte(req_stream_id);
        		packet.payload.putByte(start_stop);
        
		return packet;
        }
        
        /**
        * Decode a request_data_stream message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.req_message_rate = payload.getShort();
        	    this.target_system = payload.getByte();
        	    this.target_component = payload.getByte();
        	    this.req_stream_id = payload.getByte();
        	    this.start_stop = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_request_data_stream(){
    	msgid = MAVLINK_MSG_ID_REQUEST_DATA_STREAM;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_request_data_stream(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_REQUEST_DATA_STREAM;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "REQUEST_DATA_STREAM");
        //Log.d("MAVLINK_MSG_ID_REQUEST_DATA_STREAM", toString());
        }
        
                  
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_REQUEST_DATA_STREAM -"+" req_message_rate:"+req_message_rate+" target_system:"+target_system+" target_component:"+target_component+" req_stream_id:"+req_stream_id+" start_stop:"+start_stop+"";
        }
        }
        