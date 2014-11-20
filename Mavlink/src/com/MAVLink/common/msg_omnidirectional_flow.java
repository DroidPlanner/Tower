        // MESSAGE OMNIDIRECTIONAL_FLOW PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Optical flow from an omnidirectional flow sensor (e.g. PX4FLOW with wide angle lens)
        */
        public class msg_omnidirectional_flow extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_OMNIDIRECTIONAL_FLOW = 106;
        public static final int MAVLINK_MSG_LENGTH = 54;
        private static final long serialVersionUID = MAVLINK_MSG_ID_OMNIDIRECTIONAL_FLOW;
        
        
         	/**
        * Timestamp (microseconds, synced to UNIX time or since system boot)
        */
        public long time_usec;
         	/**
        * Front distance in meters. Positive value (including zero): distance known. Negative value: Unknown distance
        */
        public float front_distance_m;
         	/**
        * Flow in deci pixels (1 = 0.1 pixel) on left hemisphere
        */
        public short left[] = new short[10];
         	/**
        * Flow in deci pixels (1 = 0.1 pixel) on right hemisphere
        */
        public short right[] = new short[10];
         	/**
        * Sensor ID
        */
        public byte sensor_id;
         	/**
        * Optical flow quality / confidence. 0: bad, 255: maximum quality
        */
        public byte quality;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_OMNIDIRECTIONAL_FLOW;
        		packet.payload.putLong(time_usec);
        		packet.payload.putFloat(front_distance_m);
        		 for (int i = 0; i < left.length; i++) {
                    packet.payload.putShort(left[i]);
                    }
        		 for (int i = 0; i < right.length; i++) {
                    packet.payload.putShort(right[i]);
                    }
        		packet.payload.putByte(sensor_id);
        		packet.payload.putByte(quality);
        
		return packet;
        }
        
        /**
        * Decode a omnidirectional_flow message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_usec = payload.getLong();
        	    this.front_distance_m = payload.getFloat();
        	     for (int i = 0; i < this.left.length; i++) {
                    this.left[i] = payload.getShort();
                    }
        	     for (int i = 0; i < this.right.length; i++) {
                    this.right[i] = payload.getShort();
                    }
        	    this.sensor_id = payload.getByte();
        	    this.quality = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_omnidirectional_flow(){
    	msgid = MAVLINK_MSG_ID_OMNIDIRECTIONAL_FLOW;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_omnidirectional_flow(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_OMNIDIRECTIONAL_FLOW;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "OMNIDIRECTIONAL_FLOW");
        //Log.d("MAVLINK_MSG_ID_OMNIDIRECTIONAL_FLOW", toString());
        }
        
                    
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_OMNIDIRECTIONAL_FLOW -"+" time_usec:"+time_usec+" front_distance_m:"+front_distance_m+" left:"+left+" right:"+right+" sensor_id:"+sensor_id+" quality:"+quality+"";
        }
        }
        