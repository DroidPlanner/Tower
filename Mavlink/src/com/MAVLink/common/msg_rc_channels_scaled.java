        // MESSAGE RC_CHANNELS_SCALED PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * The scaled values of the RC channels received. (-100%) -10000, (0%) 0, (100%) 10000. Channels that are inactive should be set to UINT16_MAX.
        */
        public class msg_rc_channels_scaled extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_RC_CHANNELS_SCALED = 34;
        public static final int MAVLINK_MSG_LENGTH = 22;
        private static final long serialVersionUID = MAVLINK_MSG_ID_RC_CHANNELS_SCALED;
        
        
         	/**
        * Timestamp (milliseconds since system boot)
        */
        public int time_boot_ms;
         	/**
        * RC channel 1 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
        */
        public short chan1_scaled;
         	/**
        * RC channel 2 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
        */
        public short chan2_scaled;
         	/**
        * RC channel 3 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
        */
        public short chan3_scaled;
         	/**
        * RC channel 4 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
        */
        public short chan4_scaled;
         	/**
        * RC channel 5 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
        */
        public short chan5_scaled;
         	/**
        * RC channel 6 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
        */
        public short chan6_scaled;
         	/**
        * RC channel 7 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
        */
        public short chan7_scaled;
         	/**
        * RC channel 8 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
        */
        public short chan8_scaled;
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
		packet.msgid = MAVLINK_MSG_ID_RC_CHANNELS_SCALED;
        		packet.payload.putInt(time_boot_ms);
        		packet.payload.putShort(chan1_scaled);
        		packet.payload.putShort(chan2_scaled);
        		packet.payload.putShort(chan3_scaled);
        		packet.payload.putShort(chan4_scaled);
        		packet.payload.putShort(chan5_scaled);
        		packet.payload.putShort(chan6_scaled);
        		packet.payload.putShort(chan7_scaled);
        		packet.payload.putShort(chan8_scaled);
        		packet.payload.putByte(port);
        		packet.payload.putByte(rssi);
        
		return packet;
        }
        
        /**
        * Decode a rc_channels_scaled message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_boot_ms = payload.getInt();
        	    this.chan1_scaled = payload.getShort();
        	    this.chan2_scaled = payload.getShort();
        	    this.chan3_scaled = payload.getShort();
        	    this.chan4_scaled = payload.getShort();
        	    this.chan5_scaled = payload.getShort();
        	    this.chan6_scaled = payload.getShort();
        	    this.chan7_scaled = payload.getShort();
        	    this.chan8_scaled = payload.getShort();
        	    this.port = payload.getByte();
        	    this.rssi = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_rc_channels_scaled(){
    	msgid = MAVLINK_MSG_ID_RC_CHANNELS_SCALED;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_rc_channels_scaled(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_RC_CHANNELS_SCALED;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "RC_CHANNELS_SCALED");
        //Log.d("MAVLINK_MSG_ID_RC_CHANNELS_SCALED", toString());
        }
        
                              
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_RC_CHANNELS_SCALED -"+" time_boot_ms:"+time_boot_ms+" chan1_scaled:"+chan1_scaled+" chan2_scaled:"+chan2_scaled+" chan3_scaled:"+chan3_scaled+" chan4_scaled:"+chan4_scaled+" chan5_scaled:"+chan5_scaled+" chan6_scaled:"+chan6_scaled+" chan7_scaled:"+chan7_scaled+" chan8_scaled:"+chan8_scaled+" port:"+port+" rssi:"+rssi+"";
        }
        }
        