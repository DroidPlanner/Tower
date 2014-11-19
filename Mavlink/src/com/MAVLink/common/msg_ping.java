        // MESSAGE PING PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * A ping message either requesting or responding to a ping. This allows to measure the system latencies, including serial port, radio modem and UDP connections.
        */
        public class msg_ping extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_PING = 4;
        public static final int MAVLINK_MSG_LENGTH = 14;
        private static final long serialVersionUID = MAVLINK_MSG_ID_PING;
        
        
         	/**
        * Unix timestamp in microseconds
        */
        public long time_usec;
         	/**
        * PING sequence
        */
        public int seq;
         	/**
        * 0: request ping from all receiving systems, if greater than 0: message is a ping response and number is the system id of the requesting system
        */
        public byte target_system;
         	/**
        * 0: request ping from all receiving components, if greater than 0: message is a ping response and number is the system id of the requesting system
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
		packet.msgid = MAVLINK_MSG_ID_PING;
        		packet.payload.putLong(time_usec);
        		packet.payload.putInt(seq);
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        
		return packet;
        }
        
        /**
        * Decode a ping message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_usec = payload.getLong();
        	    this.seq = payload.getInt();
        	    this.target_system = payload.getByte();
        	    this.target_component = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_ping(){
    	msgid = MAVLINK_MSG_ID_PING;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_ping(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_PING;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "PING");
        //Log.d("MAVLINK_MSG_ID_PING", toString());
        }
        
                
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_PING -"+" time_usec:"+time_usec+" seq:"+seq+" target_system:"+target_system+" target_component:"+target_component+"";
        }
        }
        