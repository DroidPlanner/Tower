        // MESSAGE GPS_STATUS PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * The positioning status, as reported by GPS. This message is intended to display status information about each satellite visible to the receiver. See message GLOBAL_POSITION for the global position estimate. This message can contain information for up to 20 satellites.
        */
        public class msg_gps_status extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_GPS_STATUS = 25;
        public static final int MAVLINK_MSG_LENGTH = 101;
        private static final long serialVersionUID = MAVLINK_MSG_ID_GPS_STATUS;
        
        
         	/**
        * Number of satellites visible
        */
        public byte satellites_visible;
         	/**
        * Global satellite ID
        */
        public byte satellite_prn[] = new byte[20];
         	/**
        * 0: Satellite not used, 1: used for localization
        */
        public byte satellite_used[] = new byte[20];
         	/**
        * Elevation (0: right on top of receiver, 90: on the horizon) of satellite
        */
        public byte satellite_elevation[] = new byte[20];
         	/**
        * Direction of satellite, 0: 0 deg, 255: 360 deg.
        */
        public byte satellite_azimuth[] = new byte[20];
         	/**
        * Signal to noise ratio of satellite
        */
        public byte satellite_snr[] = new byte[20];
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_GPS_STATUS;
        		packet.payload.putByte(satellites_visible);
        		 for (int i = 0; i < satellite_prn.length; i++) {
                    packet.payload.putByte(satellite_prn[i]);
                    }
        		 for (int i = 0; i < satellite_used.length; i++) {
                    packet.payload.putByte(satellite_used[i]);
                    }
        		 for (int i = 0; i < satellite_elevation.length; i++) {
                    packet.payload.putByte(satellite_elevation[i]);
                    }
        		 for (int i = 0; i < satellite_azimuth.length; i++) {
                    packet.payload.putByte(satellite_azimuth[i]);
                    }
        		 for (int i = 0; i < satellite_snr.length; i++) {
                    packet.payload.putByte(satellite_snr[i]);
                    }
        
		return packet;
        }
        
        /**
        * Decode a gps_status message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.satellites_visible = payload.getByte();
        	     for (int i = 0; i < this.satellite_prn.length; i++) {
                    this.satellite_prn[i] = payload.getByte();
                    }
        	     for (int i = 0; i < this.satellite_used.length; i++) {
                    this.satellite_used[i] = payload.getByte();
                    }
        	     for (int i = 0; i < this.satellite_elevation.length; i++) {
                    this.satellite_elevation[i] = payload.getByte();
                    }
        	     for (int i = 0; i < this.satellite_azimuth.length; i++) {
                    this.satellite_azimuth[i] = payload.getByte();
                    }
        	     for (int i = 0; i < this.satellite_snr.length; i++) {
                    this.satellite_snr[i] = payload.getByte();
                    }
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_gps_status(){
    	msgid = MAVLINK_MSG_ID_GPS_STATUS;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_gps_status(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GPS_STATUS;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "GPS_STATUS");
        //Log.d("MAVLINK_MSG_ID_GPS_STATUS", toString());
        }
        
                    
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_GPS_STATUS -"+" satellites_visible:"+satellites_visible+" satellite_prn:"+satellite_prn+" satellite_used:"+satellite_used+" satellite_elevation:"+satellite_elevation+" satellite_azimuth:"+satellite_azimuth+" satellite_snr:"+satellite_snr+"";
        }
        }
        