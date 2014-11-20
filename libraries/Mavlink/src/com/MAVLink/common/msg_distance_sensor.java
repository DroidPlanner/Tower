        // MESSAGE DISTANCE_SENSOR PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * 
        */
        public class msg_distance_sensor extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_DISTANCE_SENSOR = 132;
        public static final int MAVLINK_MSG_LENGTH = 14;
        private static final long serialVersionUID = MAVLINK_MSG_ID_DISTANCE_SENSOR;
        
        
         	/**
        * Time since system boot
        */
        public int time_boot_ms;
         	/**
        * Minimum distance the sensor can measure in centimeters
        */
        public short min_distance;
         	/**
        * Maximum distance the sensor can measure in centimeters
        */
        public short max_distance;
         	/**
        * Current distance reading
        */
        public short current_distance;
         	/**
        * Type from MAV_DISTANCE_SENSOR enum.
        */
        public byte type;
         	/**
        * Onboard ID of the sensor
        */
        public byte id;
         	/**
        * Direction the sensor faces from FIXME enum.
        */
        public byte orientation;
         	/**
        * Measurement covariance in centimeters, 0 for unknown / invalid readings
        */
        public byte covariance;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_DISTANCE_SENSOR;
        		packet.payload.putInt(time_boot_ms);
        		packet.payload.putShort(min_distance);
        		packet.payload.putShort(max_distance);
        		packet.payload.putShort(current_distance);
        		packet.payload.putByte(type);
        		packet.payload.putByte(id);
        		packet.payload.putByte(orientation);
        		packet.payload.putByte(covariance);
        
		return packet;
        }
        
        /**
        * Decode a distance_sensor message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_boot_ms = payload.getInt();
        	    this.min_distance = payload.getShort();
        	    this.max_distance = payload.getShort();
        	    this.current_distance = payload.getShort();
        	    this.type = payload.getByte();
        	    this.id = payload.getByte();
        	    this.orientation = payload.getByte();
        	    this.covariance = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_distance_sensor(){
    	msgid = MAVLINK_MSG_ID_DISTANCE_SENSOR;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_distance_sensor(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_DISTANCE_SENSOR;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "DISTANCE_SENSOR");
        //Log.d("MAVLINK_MSG_ID_DISTANCE_SENSOR", toString());
        }
        
                        
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_DISTANCE_SENSOR -"+" time_boot_ms:"+time_boot_ms+" min_distance:"+min_distance+" max_distance:"+max_distance+" current_distance:"+current_distance+" type:"+type+" id:"+id+" orientation:"+orientation+" covariance:"+covariance+"";
        }
        }
        