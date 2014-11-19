        // MESSAGE MISSION_CURRENT PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Message that announces the sequence number of the current active mission item. The MAV will fly towards this mission item.
        */
        public class msg_mission_current extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_MISSION_CURRENT = 42;
        public static final int MAVLINK_MSG_LENGTH = 2;
        private static final long serialVersionUID = MAVLINK_MSG_ID_MISSION_CURRENT;
        
        
         	/**
        * Sequence
        */
        public short seq;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_MISSION_CURRENT;
        		packet.payload.putShort(seq);
        
		return packet;
        }
        
        /**
        * Decode a mission_current message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.seq = payload.getShort();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_mission_current(){
    	msgid = MAVLINK_MSG_ID_MISSION_CURRENT;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_mission_current(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_MISSION_CURRENT;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "MISSION_CURRENT");
        //Log.d("MAVLINK_MSG_ID_MISSION_CURRENT", toString());
        }
        
          
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_MISSION_CURRENT -"+" seq:"+seq+"";
        }
        }
        