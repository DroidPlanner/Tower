        // MESSAGE SAFETY_SET_ALLOWED_AREA PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Set a safety zone (volume), which is defined by two corners of a cube. This message can be used to tell the MAV which setpoints/MISSIONs to accept and which to reject. Safety areas are often enforced by national or competition regulations.
        */
        public class msg_safety_set_allowed_area extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_SAFETY_SET_ALLOWED_AREA = 54;
        public static final int MAVLINK_MSG_LENGTH = 27;
        private static final long serialVersionUID = MAVLINK_MSG_ID_SAFETY_SET_ALLOWED_AREA;
        
        
         	/**
        * x position 1 / Latitude 1
        */
        public float p1x;
         	/**
        * y position 1 / Longitude 1
        */
        public float p1y;
         	/**
        * z position 1 / Altitude 1
        */
        public float p1z;
         	/**
        * x position 2 / Latitude 2
        */
        public float p2x;
         	/**
        * y position 2 / Longitude 2
        */
        public float p2y;
         	/**
        * z position 2 / Altitude 2
        */
        public float p2z;
         	/**
        * System ID
        */
        public byte target_system;
         	/**
        * Component ID
        */
        public byte target_component;
         	/**
        * Coordinate frame, as defined by MAV_FRAME enum in mavlink_types.h. Can be either global, GPS, right-handed with Z axis up or local, right handed, Z axis down.
        */
        public byte frame;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_SAFETY_SET_ALLOWED_AREA;
        		packet.payload.putFloat(p1x);
        		packet.payload.putFloat(p1y);
        		packet.payload.putFloat(p1z);
        		packet.payload.putFloat(p2x);
        		packet.payload.putFloat(p2y);
        		packet.payload.putFloat(p2z);
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        		packet.payload.putByte(frame);
        
		return packet;
        }
        
        /**
        * Decode a safety_set_allowed_area message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.p1x = payload.getFloat();
        	    this.p1y = payload.getFloat();
        	    this.p1z = payload.getFloat();
        	    this.p2x = payload.getFloat();
        	    this.p2y = payload.getFloat();
        	    this.p2z = payload.getFloat();
        	    this.target_system = payload.getByte();
        	    this.target_component = payload.getByte();
        	    this.frame = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_safety_set_allowed_area(){
    	msgid = MAVLINK_MSG_ID_SAFETY_SET_ALLOWED_AREA;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_safety_set_allowed_area(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SAFETY_SET_ALLOWED_AREA;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "SAFETY_SET_ALLOWED_AREA");
        //Log.d("MAVLINK_MSG_ID_SAFETY_SET_ALLOWED_AREA", toString());
        }
        
                          
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_SAFETY_SET_ALLOWED_AREA -"+" p1x:"+p1x+" p1y:"+p1y+" p1z:"+p1z+" p2x:"+p2x+" p2y:"+p2y+" p2z:"+p2z+" target_system:"+target_system+" target_component:"+target_component+" frame:"+frame+"";
        }
        }
        