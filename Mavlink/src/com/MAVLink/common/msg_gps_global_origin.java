        // MESSAGE GPS_GLOBAL_ORIGIN PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Once the MAV sets a new GPS-Local correspondence, this message announces the origin (0,0,0) position
        */
        public class msg_gps_global_origin extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN = 49;
        public static final int MAVLINK_MSG_LENGTH = 12;
        private static final long serialVersionUID = MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN;
        
        
         	/**
        * Latitude (WGS84), in degrees * 1E7
        */
        public int latitude;
         	/**
        * Longitude (WGS84), in degrees * 1E7
        */
        public int longitude;
         	/**
        * Altitude (WGS84), in meters * 1000 (positive for up)
        */
        public int altitude;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN;
        		packet.payload.putInt(latitude);
        		packet.payload.putInt(longitude);
        		packet.payload.putInt(altitude);
        
		return packet;
        }
        
        /**
        * Decode a gps_global_origin message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.latitude = payload.getInt();
        	    this.longitude = payload.getInt();
        	    this.altitude = payload.getInt();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_gps_global_origin(){
    	msgid = MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_gps_global_origin(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "GPS_GLOBAL_ORIGIN");
        //Log.d("MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN", toString());
        }
        
              
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN -"+" latitude:"+latitude+" longitude:"+longitude+" altitude:"+altitude+"";
        }
        }
        