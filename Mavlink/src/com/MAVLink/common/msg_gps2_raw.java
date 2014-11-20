        // MESSAGE GPS2_RAW PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Second GPS data. Coordinate frame is right-handed, Z-axis up (GPS frame).
        */
        public class msg_gps2_raw extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_GPS2_RAW = 124;
        public static final int MAVLINK_MSG_LENGTH = 35;
        private static final long serialVersionUID = MAVLINK_MSG_ID_GPS2_RAW;
        
        
         	/**
        * Timestamp (microseconds since UNIX epoch or microseconds since system boot)
        */
        public long time_usec;
         	/**
        * Latitude (WGS84), in degrees * 1E7
        */
        public int lat;
         	/**
        * Longitude (WGS84), in degrees * 1E7
        */
        public int lon;
         	/**
        * Altitude (WGS84), in meters * 1000 (positive for up)
        */
        public int alt;
         	/**
        * Age of DGPS info
        */
        public int dgps_age;
         	/**
        * GPS HDOP horizontal dilution of position in cm (m*100). If unknown, set to: UINT16_MAX
        */
        public short eph;
         	/**
        * GPS VDOP vertical dilution of position in cm (m*100). If unknown, set to: UINT16_MAX
        */
        public short epv;
         	/**
        * GPS ground speed (m/s * 100). If unknown, set to: UINT16_MAX
        */
        public short vel;
         	/**
        * Course over ground (NOT heading, but direction of movement) in degrees * 100, 0.0..359.99 degrees. If unknown, set to: UINT16_MAX
        */
        public short cog;
         	/**
        * 0-1: no fix, 2: 2D fix, 3: 3D fix, 4: DGPS fix, 5: RTK Fix. Some applications will not use the value of this field unless it is at least two, so always correctly fill in the fix.
        */
        public byte fix_type;
         	/**
        * Number of satellites visible. If unknown, set to 255
        */
        public byte satellites_visible;
         	/**
        * Number of DGPS satellites
        */
        public byte dgps_numch;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_GPS2_RAW;
        		packet.payload.putLong(time_usec);
        		packet.payload.putInt(lat);
        		packet.payload.putInt(lon);
        		packet.payload.putInt(alt);
        		packet.payload.putInt(dgps_age);
        		packet.payload.putShort(eph);
        		packet.payload.putShort(epv);
        		packet.payload.putShort(vel);
        		packet.payload.putShort(cog);
        		packet.payload.putByte(fix_type);
        		packet.payload.putByte(satellites_visible);
        		packet.payload.putByte(dgps_numch);
        
		return packet;
        }
        
        /**
        * Decode a gps2_raw message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_usec = payload.getLong();
        	    this.lat = payload.getInt();
        	    this.lon = payload.getInt();
        	    this.alt = payload.getInt();
        	    this.dgps_age = payload.getInt();
        	    this.eph = payload.getShort();
        	    this.epv = payload.getShort();
        	    this.vel = payload.getShort();
        	    this.cog = payload.getShort();
        	    this.fix_type = payload.getByte();
        	    this.satellites_visible = payload.getByte();
        	    this.dgps_numch = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_gps2_raw(){
    	msgid = MAVLINK_MSG_ID_GPS2_RAW;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_gps2_raw(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GPS2_RAW;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "GPS2_RAW");
        //Log.d("MAVLINK_MSG_ID_GPS2_RAW", toString());
        }
        
                                
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_GPS2_RAW -"+" time_usec:"+time_usec+" lat:"+lat+" lon:"+lon+" alt:"+alt+" dgps_age:"+dgps_age+" eph:"+eph+" epv:"+epv+" vel:"+vel+" cog:"+cog+" fix_type:"+fix_type+" satellites_visible:"+satellites_visible+" dgps_numch:"+dgps_numch+"";
        }
        }
        