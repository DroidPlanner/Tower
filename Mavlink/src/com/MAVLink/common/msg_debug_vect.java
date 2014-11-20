        // MESSAGE DEBUG_VECT PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * 
        */
        public class msg_debug_vect extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_DEBUG_VECT = 250;
        public static final int MAVLINK_MSG_LENGTH = 30;
        private static final long serialVersionUID = MAVLINK_MSG_ID_DEBUG_VECT;
        
        
         	/**
        * Timestamp
        */
        public long time_usec;
         	/**
        * x
        */
        public float x;
         	/**
        * y
        */
        public float y;
         	/**
        * z
        */
        public float z;
         	/**
        * Name
        */
        public byte name[] = new byte[10];
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_DEBUG_VECT;
        		packet.payload.putLong(time_usec);
        		packet.payload.putFloat(x);
        		packet.payload.putFloat(y);
        		packet.payload.putFloat(z);
        		 for (int i = 0; i < name.length; i++) {
                    packet.payload.putByte(name[i]);
                    }
        
		return packet;
        }
        
        /**
        * Decode a debug_vect message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_usec = payload.getLong();
        	    this.x = payload.getFloat();
        	    this.y = payload.getFloat();
        	    this.z = payload.getFloat();
        	     for (int i = 0; i < this.name.length; i++) {
                    this.name[i] = payload.getByte();
                    }
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_debug_vect(){
    	msgid = MAVLINK_MSG_ID_DEBUG_VECT;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_debug_vect(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_DEBUG_VECT;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "DEBUG_VECT");
        //Log.d("MAVLINK_MSG_ID_DEBUG_VECT", toString());
        }
        
                 /**
                        * Sets the buffer of this message with a string, adds the necessary padding
                        */
                        public void setName(String str) {
                        int len = Math.min(str.length(), 10);
                        for (int i=0; i<len; i++) {
                        name[i] = (byte) str.charAt(i);
                        }
                        for (int i=len; i<10; i++) {			// padding for the rest of the buffer
                        name[i] = 0;
                        }
                        }
                        
                        /**
                        * Gets the message, formated as a string
                        */
                        public String getName() {
                        String result = "";
                        for (int i = 0; i < 10; i++) {
                        if (name[i] != 0)
                        result = result + (char) name[i];
                        else
                        break;
                        }
                        return result;
                        
                        } 
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_DEBUG_VECT -"+" time_usec:"+time_usec+" x:"+x+" y:"+y+" z:"+z+" name:"+name+"";
        }
        }
        