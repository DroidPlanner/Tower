        // MESSAGE GPS_INJECT_DATA PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * data for injecting into the onboard GPS (used for DGPS)
        */
        public class msg_gps_inject_data extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_GPS_INJECT_DATA = 123;
        public static final int MAVLINK_MSG_LENGTH = 113;
        private static final long serialVersionUID = MAVLINK_MSG_ID_GPS_INJECT_DATA;
        
        
         	/**
        * System ID
        */
        public byte target_system;
         	/**
        * Component ID
        */
        public byte target_component;
         	/**
        * data length
        */
        public byte len;
         	/**
        * raw data (110 is enough for 12 satellites of RTCMv2)
        */
        public byte data[] = new byte[110];
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_GPS_INJECT_DATA;
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        		packet.payload.putByte(len);
        		 for (int i = 0; i < data.length; i++) {
                    packet.payload.putByte(data[i]);
                    }
        
		return packet;
        }
        
        /**
        * Decode a gps_inject_data message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.target_system = payload.getByte();
        	    this.target_component = payload.getByte();
        	    this.len = payload.getByte();
        	     for (int i = 0; i < this.data.length; i++) {
                    this.data[i] = payload.getByte();
                    }
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_gps_inject_data(){
    	msgid = MAVLINK_MSG_ID_GPS_INJECT_DATA;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_gps_inject_data(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GPS_INJECT_DATA;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "GPS_INJECT_DATA");
        //Log.d("MAVLINK_MSG_ID_GPS_INJECT_DATA", toString());
        }
        
                
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_GPS_INJECT_DATA -"+" target_system:"+target_system+" target_component:"+target_component+" len:"+len+" data:"+data+"";
        }
        }
        