        // MESSAGE SCALED_PRESSURE PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * The pressure readings for the typical setup of one absolute and differential pressure sensor. The units are as specified in each field.
        */
        public class msg_scaled_pressure extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_SCALED_PRESSURE = 29;
        public static final int MAVLINK_MSG_LENGTH = 14;
        private static final long serialVersionUID = MAVLINK_MSG_ID_SCALED_PRESSURE;
        
        
         	/**
        * Timestamp (milliseconds since system boot)
        */
        public int time_boot_ms;
         	/**
        * Absolute pressure (hectopascal)
        */
        public float press_abs;
         	/**
        * Differential pressure 1 (hectopascal)
        */
        public float press_diff;
         	/**
        * Temperature measurement (0.01 degrees celsius)
        */
        public short temperature;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_SCALED_PRESSURE;
        		packet.payload.putInt(time_boot_ms);
        		packet.payload.putFloat(press_abs);
        		packet.payload.putFloat(press_diff);
        		packet.payload.putShort(temperature);
        
		return packet;
        }
        
        /**
        * Decode a scaled_pressure message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_boot_ms = payload.getInt();
        	    this.press_abs = payload.getFloat();
        	    this.press_diff = payload.getFloat();
        	    this.temperature = payload.getShort();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_scaled_pressure(){
    	msgid = MAVLINK_MSG_ID_SCALED_PRESSURE;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_scaled_pressure(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SCALED_PRESSURE;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "SCALED_PRESSURE");
        //Log.d("MAVLINK_MSG_ID_SCALED_PRESSURE", toString());
        }
        
                
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_SCALED_PRESSURE -"+" time_boot_ms:"+time_boot_ms+" press_abs:"+press_abs+" press_diff:"+press_diff+" temperature:"+temperature+"";
        }
        }
        