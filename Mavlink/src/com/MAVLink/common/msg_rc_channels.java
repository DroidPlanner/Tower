        // MESSAGE RC_CHANNELS PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * The PPM values of the RC channels received. The standard PPM modulation is as follows: 1000 microseconds: 0%, 2000 microseconds: 100%. Individual receivers/transmitters might violate this specification.
        */
        public class msg_rc_channels extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_RC_CHANNELS = 65;
        public static final int MAVLINK_MSG_LENGTH = 42;
        private static final long serialVersionUID = MAVLINK_MSG_ID_RC_CHANNELS;
        
        
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
        * RC channel 9 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan9_raw;
         	/**
        * RC channel 10 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan10_raw;
         	/**
        * RC channel 11 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan11_raw;
         	/**
        * RC channel 12 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan12_raw;
         	/**
        * RC channel 13 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan13_raw;
         	/**
        * RC channel 14 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan14_raw;
         	/**
        * RC channel 15 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan15_raw;
         	/**
        * RC channel 16 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan16_raw;
         	/**
        * RC channel 17 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan17_raw;
         	/**
        * RC channel 18 value, in microseconds. A value of UINT16_MAX implies the channel is unused.
        */
        public short chan18_raw;
         	/**
        * Total number of RC channels being received. This can be larger than 18, indicating that more channels are available but not given in this message. This value should be 0 when no RC channels are available.
        */
        public byte chancount;
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
		packet.msgid = MAVLINK_MSG_ID_RC_CHANNELS;
        		packet.payload.putInt(time_boot_ms);
        		packet.payload.putShort(chan1_raw);
        		packet.payload.putShort(chan2_raw);
        		packet.payload.putShort(chan3_raw);
        		packet.payload.putShort(chan4_raw);
        		packet.payload.putShort(chan5_raw);
        		packet.payload.putShort(chan6_raw);
        		packet.payload.putShort(chan7_raw);
        		packet.payload.putShort(chan8_raw);
        		packet.payload.putShort(chan9_raw);
        		packet.payload.putShort(chan10_raw);
        		packet.payload.putShort(chan11_raw);
        		packet.payload.putShort(chan12_raw);
        		packet.payload.putShort(chan13_raw);
        		packet.payload.putShort(chan14_raw);
        		packet.payload.putShort(chan15_raw);
        		packet.payload.putShort(chan16_raw);
        		packet.payload.putShort(chan17_raw);
        		packet.payload.putShort(chan18_raw);
        		packet.payload.putByte(chancount);
        		packet.payload.putByte(rssi);
        
		return packet;
        }
        
        /**
        * Decode a rc_channels message into this class fields
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
        	    this.chan9_raw = payload.getShort();
        	    this.chan10_raw = payload.getShort();
        	    this.chan11_raw = payload.getShort();
        	    this.chan12_raw = payload.getShort();
        	    this.chan13_raw = payload.getShort();
        	    this.chan14_raw = payload.getShort();
        	    this.chan15_raw = payload.getShort();
        	    this.chan16_raw = payload.getShort();
        	    this.chan17_raw = payload.getShort();
        	    this.chan18_raw = payload.getShort();
        	    this.chancount = payload.getByte();
        	    this.rssi = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_rc_channels(){
    	msgid = MAVLINK_MSG_ID_RC_CHANNELS;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_rc_channels(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_RC_CHANNELS;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "RC_CHANNELS");
        //Log.d("MAVLINK_MSG_ID_RC_CHANNELS", toString());
        }
        
                                                  
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_RC_CHANNELS -"+" time_boot_ms:"+time_boot_ms+" chan1_raw:"+chan1_raw+" chan2_raw:"+chan2_raw+" chan3_raw:"+chan3_raw+" chan4_raw:"+chan4_raw+" chan5_raw:"+chan5_raw+" chan6_raw:"+chan6_raw+" chan7_raw:"+chan7_raw+" chan8_raw:"+chan8_raw+" chan9_raw:"+chan9_raw+" chan10_raw:"+chan10_raw+" chan11_raw:"+chan11_raw+" chan12_raw:"+chan12_raw+" chan13_raw:"+chan13_raw+" chan14_raw:"+chan14_raw+" chan15_raw:"+chan15_raw+" chan16_raw:"+chan16_raw+" chan17_raw:"+chan17_raw+" chan18_raw:"+chan18_raw+" chancount:"+chancount+" rssi:"+rssi+"";
        }
        }
        