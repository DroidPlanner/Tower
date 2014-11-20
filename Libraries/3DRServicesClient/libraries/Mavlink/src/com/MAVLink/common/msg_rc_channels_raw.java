        // MESSAGE RC_CHANNELS_RAW PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * The RAW values of the RC channels received. The standard PPM modulation is as follows: 1000 microseconds: 0%, 2000 microseconds: 100%. Individual receivers/transmitters might violate this specification.
        */
        public class msg_rc_channels_raw extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_RC_CHANNELS_RAW = 35;
        public static final int MAVLINK_MSG_LENGTH = 22;
        private static final long serialVersionUID = MAVLINK_MSG_ID_RC_CHANNELS_RAW;
        
        
         	/**
        * Timestamp (milliseconds since system boot)
        */
        public int time_boot_ms;
         	/**
        * RC channel 1 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan1_raw;
         	/**
        * RC channel 2 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan2_raw;
         	/**
        * RC channel 3 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan3_raw;
         	/**
        * RC channel 4 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan4_raw;
         	/**
        * RC channel 5 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan5_raw;
         	/**
        * RC channel 6 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan6_raw;
         	/**
        * RC channel 7 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan7_raw;
         	/**
        * RC channel 8 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan8_raw;
         	/**
        * Servo output port (set of 8 outputs = 1 port). Most MAVs will just use one, but this allows for more than 8 servos.
        */
        public byte port;
         	/**
        * Receive signal strength indicator, 0: 0%, 100: 100%, 255: invalid/unknown.
        */
        public byte rssi;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_RC_CHANNELS_RAW;
        		packet.payload.putInt(time_boot_ms);
        		packet.payload.putShort(chan1_raw);
        		packet.payload.putShort(chan2_raw);
        		packet.payload.putShort(chan3_raw);
        		packet.payload.putShort(chan4_raw);
        		packet.payload.putShort(chan5_raw);
        		packet.payload.putShort(chan6_raw);
        		packet.payload.putShort(chan7_raw);
        		packet.payload.putShort(chan8_raw);
        		packet.payload.putByte(port);
        		packet.payload.putByte(rssi);
        
		return packet;
        }
        
        /**
        * Decode a rc_channels_raw message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_boot_ms = payload.getInt();
        	    this.chan1_raw = payload.getShort();
        	    this.chan2_raw = payload.getShort();
        	    this.chan3_raw = payload.getShort();
        	    this.chan4_raw = payload.getShort();
        	    this.chan5_raw = payload.getShort();
        	    this.chan6_raw = payload.getShort();
        	    this.chan7_raw = payload.getShort();
        	    this.chan8_raw = payload.getShort();
        	    this.port = payload.getByte();
        	    this.rssi = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_rc_channels_raw(){
    	msgid = MAVLINK_MSG_ID_RC_CHANNELS_RAW;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_rc_channels_raw(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_RC_CHANNELS_RAW;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "RC_CHANNELS_RAW");
        //Log.d("MAVLINK_MSG_ID_RC_CHANNELS_RAW", toString());
        }
        
                              
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_RC_CHANNELS_RAW -"+" time_boot_ms:"+time_boot_ms+" chan1_raw:"+chan1_raw+" chan2_raw:"+chan2_raw+" chan3_raw:"+chan3_raw+" chan4_raw:"+chan4_raw+" chan5_raw:"+chan5_raw+" chan6_raw:"+chan6_raw+" chan7_raw:"+chan7_raw+" chan8_raw:"+chan8_raw+" port:"+port+" rssi:"+rssi+"";
        }
        }
        