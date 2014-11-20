        // MESSAGE RALLY_POINT PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * A rally point. Used to set a point when from GCS -> MAV. Also used to return a point from MAV -> GCS
        */
        public class msg_rally_point extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_RALLY_POINT = 175;
        public static final int MAVLINK_MSG_LENGTH = 19;
        private static final long serialVersionUID = MAVLINK_MSG_ID_RALLY_POINT;
        
        
         	/**
        * Latitude of point in degrees * 1E7
        */
        public int lat;
         	/**
        * Longitude of point in degrees * 1E7
        */
        public int lng;
         	/**
        * Transit / loiter altitude in meters relative to home
        */
        public short alt;
         	/**
        * Break altitude in meters relative to home
        */
        public short break_alt;
         	/**
        * Heading to aim for when landing. In centi-degrees.
        */
        public short land_dir;
         	/**
        * System ID
        */
        public byte target_system;
         	/**
        * Component ID
        */
        public byte target_component;
         	/**
        * point index (first point is 0)
        */
        public byte idx;
         	/**
        * total number of points (for sanity checking)
        */
        public byte count;
         	/**
        * See RALLY_FLAGS enum for definition of the bitmask.
        */
        public byte flags;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_RALLY_POINT;
        		packet.payload.putInt(lat);
        		packet.payload.putInt(lng);
        		packet.payload.putShort(alt);
        		packet.payload.putShort(break_alt);
        		packet.payload.putShort(land_dir);
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        		packet.payload.putByte(idx);
        		packet.payload.putByte(count);
        		packet.payload.putByte(flags);
        
		return packet;
        }
        
        /**
        * Decode a rally_point message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.lat = payload.getInt();
        	    this.lng = payload.getInt();
        	    this.alt = payload.getShort();
        	    this.break_alt = payload.getShort();
        	    this.land_dir = payload.getShort();
        	    this.target_system = payload.getByte();
        	    this.target_component = payload.getByte();
        	    this.idx = payload.getByte();
        	    this.count = payload.getByte();
        	    this.flags = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_rally_point(){
    	msgid = MAVLINK_MSG_ID_RALLY_POINT;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_rally_point(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_RALLY_POINT;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "RALLY_POINT");
        //Log.d("MAVLINK_MSG_ID_RALLY_POINT", toString());
        }
        
                            
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_RALLY_POINT -"+" lat:"+lat+" lng:"+lng+" alt:"+alt+" break_alt:"+break_alt+" land_dir:"+land_dir+" target_system:"+target_system+" target_component:"+target_component+" idx:"+idx+" count:"+count+" flags:"+flags+"";
        }
        }
        