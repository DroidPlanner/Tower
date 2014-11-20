        // MESSAGE TERRAIN_CHECK PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Request that the vehicle report terrain height at the given location. Used by GCS to check if vehicle has all terrain data needed for a mission.
        */
        public class msg_terrain_check extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_TERRAIN_CHECK = 135;
        public static final int MAVLINK_MSG_LENGTH = 8;
        private static final long serialVersionUID = MAVLINK_MSG_ID_TERRAIN_CHECK;
        
        
         	/**
        * Latitude (degrees *10^7)
        */
        public int lat;
         	/**
        * Longitude (degrees *10^7)
        */
        public int lon;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_TERRAIN_CHECK;
        		packet.payload.putInt(lat);
        		packet.payload.putInt(lon);
        
		return packet;
        }
        
        /**
        * Decode a terrain_check message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.lat = payload.getInt();
        	    this.lon = payload.getInt();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_terrain_check(){
    	msgid = MAVLINK_MSG_ID_TERRAIN_CHECK;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_terrain_check(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_TERRAIN_CHECK;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "TERRAIN_CHECK");
        //Log.d("MAVLINK_MSG_ID_TERRAIN_CHECK", toString());
        }
        
            
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_TERRAIN_CHECK -"+" lat:"+lat+" lon:"+lon+"";
        }
        }
        