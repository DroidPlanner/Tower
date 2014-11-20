        // MESSAGE MISSION_SET_CURRENT PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Set the mission item with sequence number seq as current item. This means that the MAV will continue to this mission item on the shortest path (not following the mission items in-between).
        */
        public class msg_mission_set_current extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_MISSION_SET_CURRENT = 41;
        public static final int MAVLINK_MSG_LENGTH = 4;
        private static final long serialVersionUID = MAVLINK_MSG_ID_MISSION_SET_CURRENT;
        
        
         	/**
        * Sequence
        */
        public short seq;
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
		packet.msgid = MAVLINK_MSG_ID_MISSION_SET_CURRENT;
        		packet.payload.putShort(seq);
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        
		return packet;
        }
        
        /**
        * Decode a mission_set_current message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.seq = payload.getShort();
        	    this.target_system = payload.getByte();
        	    this.target_component = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_mission_set_current(){
    	msgid = MAVLINK_MSG_ID_MISSION_SET_CURRENT;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_mission_set_current(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_MISSION_SET_CURRENT;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "MISSION_SET_CURRENT");
        //Log.d("MAVLINK_MSG_ID_MISSION_SET_CURRENT", toString());
        }
        
              
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_MISSION_SET_CURRENT -"+" seq:"+seq+" target_system:"+target_system+" target_component:"+target_component+"";
        }
        }
        