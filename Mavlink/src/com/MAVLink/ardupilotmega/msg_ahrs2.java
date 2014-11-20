        // MESSAGE AHRS2 PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Status of secondary AHRS filter if available
        */
        public class msg_ahrs2 extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_AHRS2 = 178;
        public static final int MAVLINK_MSG_LENGTH = 24;
        private static final long serialVersionUID = MAVLINK_MSG_ID_AHRS2;
        
        
         	/**
        * Roll angle (rad)
        */
        public float roll;
         	/**
        * Pitch angle (rad)
        */
        public float pitch;
         	/**
        * Yaw angle (rad)
        */
        public float yaw;
         	/**
        * Altitude (MSL)
        */
        public float altitude;
         	/**
        * Latitude in degrees * 1E7
        */
        public int lat;
         	/**
        * Longitude in degrees * 1E7
        */
        public int lng;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_AHRS2;
        		packet.payload.putFloat(roll);
        		packet.payload.putFloat(pitch);
        		packet.payload.putFloat(yaw);
        		packet.payload.putFloat(altitude);
        		packet.payload.putInt(lat);
        		packet.payload.putInt(lng);
        
		return packet;
        }
        
        /**
        * Decode a ahrs2 message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.roll = payload.getFloat();
        	    this.pitch = payload.getFloat();
        	    this.yaw = payload.getFloat();
        	    this.altitude = payload.getFloat();
        	    this.lat = payload.getInt();
        	    this.lng = payload.getInt();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_ahrs2(){
    	msgid = MAVLINK_MSG_ID_AHRS2;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_ahrs2(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_AHRS2;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "AHRS2");
        //Log.d("MAVLINK_MSG_ID_AHRS2", toString());
        }
        
                    
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_AHRS2 -"+" roll:"+roll+" pitch:"+pitch+" yaw:"+yaw+" altitude:"+altitude+" lat:"+lat+" lng:"+lng+"";
        }
        }
        