        // MESSAGE LOCAL_POSITION_NED PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * The filtered local position (e.g. fused computer vision and accelerometers). Coordinate frame is right-handed, Z-axis down (aeronautical frame, NED / north-east-down convention)
        */
        public class msg_local_position_ned extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_LOCAL_POSITION_NED = 32;
        public static final int MAVLINK_MSG_LENGTH = 28;
        private static final long serialVersionUID = MAVLINK_MSG_ID_LOCAL_POSITION_NED;
        
        
         	/**
        * Timestamp (milliseconds since system boot)
        */
        public int time_boot_ms;
         	/**
        * X Position
        */
        public float x;
         	/**
        * Y Position
        */
        public float y;
         	/**
        * Z Position
        */
        public float z;
         	/**
        * X Speed
        */
        public float vx;
         	/**
        * Y Speed
        */
        public float vy;
         	/**
        * Z Speed
        */
        public float vz;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_LOCAL_POSITION_NED;
        		packet.payload.putInt(time_boot_ms);
        		packet.payload.putFloat(x);
        		packet.payload.putFloat(y);
        		packet.payload.putFloat(z);
        		packet.payload.putFloat(vx);
        		packet.payload.putFloat(vy);
        		packet.payload.putFloat(vz);
        
		return packet;
        }
        
        /**
        * Decode a local_position_ned message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_boot_ms = payload.getInt();
        	    this.x = payload.getFloat();
        	    this.y = payload.getFloat();
        	    this.z = payload.getFloat();
        	    this.vx = payload.getFloat();
        	    this.vy = payload.getFloat();
        	    this.vz = payload.getFloat();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_local_position_ned(){
    	msgid = MAVLINK_MSG_ID_LOCAL_POSITION_NED;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_local_position_ned(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_LOCAL_POSITION_NED;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "LOCAL_POSITION_NED");
        //Log.d("MAVLINK_MSG_ID_LOCAL_POSITION_NED", toString());
        }
        
                      
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_LOCAL_POSITION_NED -"+" time_boot_ms:"+time_boot_ms+" x:"+x+" y:"+y+" z:"+z+" vx:"+vx+" vy:"+vy+" vz:"+vz+"";
        }
        }
        