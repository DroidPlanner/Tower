        // MESSAGE MEMINFO PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * state of APM memory
        */
        public class msg_meminfo extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_MEMINFO = 152;
        public static final int MAVLINK_MSG_LENGTH = 4;
        private static final long serialVersionUID = MAVLINK_MSG_ID_MEMINFO;
        
        
         	/**
        * heap top
        */
        public short brkval;
         	/**
        * free memory
        */
        public short freemem;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_MEMINFO;
        		packet.payload.putShort(brkval);
        		packet.payload.putShort(freemem);
        
		return packet;
        }
        
        /**
        * Decode a meminfo message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.brkval = payload.getShort();
        	    this.freemem = payload.getShort();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_meminfo(){
    	msgid = MAVLINK_MSG_ID_MEMINFO;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_meminfo(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_MEMINFO;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "MEMINFO");
        //Log.d("MAVLINK_MSG_ID_MEMINFO", toString());
        }
        
            
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_MEMINFO -"+" brkval:"+brkval+" freemem:"+freemem+"";
        }
        }
        