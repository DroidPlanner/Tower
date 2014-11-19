        // MESSAGE SET_MODE PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Set the system mode, as defined by enum MAV_MODE. There is no target component id as the mode is by definition for the overall aircraft, not only for one component.
        */
        public class msg_set_mode extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_SET_MODE = 11;
        public static final int MAVLINK_MSG_LENGTH = 6;
        private static final long serialVersionUID = MAVLINK_MSG_ID_SET_MODE;
        
        
         	/**
        * The new autopilot-specific mode. This field can be ignored by an autopilot.
        */
        public int custom_mode;
         	/**
        * The system setting the mode
        */
        public byte target_system;
         	/**
        * The new base mode
        */
        public byte base_mode;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_SET_MODE;
        		packet.payload.putInt(custom_mode);
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(base_mode);
        
		return packet;
        }
        
        /**
        * Decode a set_mode message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.custom_mode = payload.getInt();
        	    this.target_system = payload.getByte();
        	    this.base_mode = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_set_mode(){
    	msgid = MAVLINK_MSG_ID_SET_MODE;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_set_mode(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SET_MODE;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "SET_MODE");
        //Log.d("MAVLINK_MSG_ID_SET_MODE", toString());
        }
        
              
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_SET_MODE -"+" custom_mode:"+custom_mode+" target_system:"+target_system+" base_mode:"+base_mode+"";
        }
        }
        