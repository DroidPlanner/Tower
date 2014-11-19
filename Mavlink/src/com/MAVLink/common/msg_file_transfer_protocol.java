        // MESSAGE FILE_TRANSFER_PROTOCOL PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * File transfer message
        */
        public class msg_file_transfer_protocol extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_FILE_TRANSFER_PROTOCOL = 110;
        public static final int MAVLINK_MSG_LENGTH = 254;
        private static final long serialVersionUID = MAVLINK_MSG_ID_FILE_TRANSFER_PROTOCOL;
        
        
         	/**
        * Network ID (0 for broadcast)
        */
        public byte target_network;
         	/**
        * System ID (0 for broadcast)
        */
        public byte target_system;
         	/**
        * Component ID (0 for broadcast)
        */
        public byte target_component;
         	/**
        * Variable length payload. The length is defined by the remaining message length when subtracting the header and other fields.  The entire content of this block is opaque unless you understand any the encoding message_type.  The particular encoding used can be extension specific and might not always be documented as part of the mavlink specification.
        */
        public byte payload[] = new byte[251];
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_FILE_TRANSFER_PROTOCOL;
        		packet.payload.putByte(target_network);
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        		 for (int i = 0; i < payload.length; i++) {
                    packet.payload.putByte(payload[i]);
                    }
        
		return packet;
        }
        
        /**
        * Decode a file_transfer_protocol message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.target_network = payload.getByte();
        	    this.target_system = payload.getByte();
        	    this.target_component = payload.getByte();
        	     for (int i = 0; i < this.payload.length; i++) {
                    this.payload[i] = payload.getByte();
                    }
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_file_transfer_protocol(){
    	msgid = MAVLINK_MSG_ID_FILE_TRANSFER_PROTOCOL;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_file_transfer_protocol(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_FILE_TRANSFER_PROTOCOL;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "FILE_TRANSFER_PROTOCOL");
        //Log.d("MAVLINK_MSG_ID_FILE_TRANSFER_PROTOCOL", toString());
        }
        
                
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_FILE_TRANSFER_PROTOCOL -"+" target_network:"+target_network+" target_system:"+target_system+" target_component:"+target_component+" payload:"+payload+"";
        }
        }
        