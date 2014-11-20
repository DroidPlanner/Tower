        // MESSAGE MISSION_ITEM_REACHED PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * A certain mission item has been reached. The system will either hold this position (or circle on the orbit) or (if the autocontinue on the WP was set) continue to the next MISSION.
        */
        public class msg_mission_item_reached extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_MISSION_ITEM_REACHED = 46;
        public static final int MAVLINK_MSG_LENGTH = 2;
        private static final long serialVersionUID = MAVLINK_MSG_ID_MISSION_ITEM_REACHED;
        
        
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
		packet.msgid = MAVLINK_MSG_ID_MISSION_ITEM_REACHED;
        		packet.payload.putShort(seq);
        
		return packet;
        }
        
        /**
        * Decode a mission_item_reached message into this class fields
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
        public msg_mission_item_reached(){
    	msgid = MAVLINK_MSG_ID_MISSION_ITEM_REACHED;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_mission_item_reached(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_MISSION_ITEM_REACHED;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "MISSION_ITEM_REACHED");
        //Log.d("MAVLINK_MSG_ID_MISSION_ITEM_REACHED", toString());
        }
        
          
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_MISSION_ITEM_REACHED -"+" seq:"+seq+"";
        }
        }
        