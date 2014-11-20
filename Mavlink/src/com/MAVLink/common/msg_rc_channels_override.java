        // MESSAGE RC_CHANNELS_OVERRIDE PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * The RAW values of the RC channels sent to the MAV to override info received from the RC radio. A value of UINT16_MAX means no change to that channel. A value of 0 means control of that channel should be released back to the RC radio. The standard PPM modulation is as follows: 1000 microseconds: 0%, 2000 microseconds: 100%. Individual receivers/transmitters might violate this specification.
        */
        public class msg_rc_channels_override extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE = 70;
        public static final int MAVLINK_MSG_LENGTH = 18;
        private static final long serialVersionUID = MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE;
        
        
         	/**
        * RC channel 1 value, in microseconds. A value of UINT16_MAX means to ignore this field.
        */
        public short chan1_raw;
         	/**
        * RC channel 2 value, in microseconds. A value of UINT16_MAX means to ignore this field.
        */
        public short chan2_raw;
         	/**
        * RC channel 3 value, in microseconds. A value of UINT16_MAX means to ignore this field.
        */
        public short chan3_raw;
         	/**
        * RC channel 4 value, in microseconds. A value of UINT16_MAX means to ignore this field.
        */
        public short chan4_raw;
         	/**
        * RC channel 5 value, in microseconds. A value of UINT16_MAX means to ignore this field.
        */
        public short chan5_raw;
         	/**
        * RC channel 6 value, in microseconds. A value of UINT16_MAX means to ignore this field.
        */
        public short chan6_raw;
         	/**
        * RC channel 7 value, in microseconds. A value of UINT16_MAX means to ignore this field.
        */
        public short chan7_raw;
         	/**
        * RC channel 8 value, in microseconds. A value of UINT16_MAX means to ignore this field.
        */
        public short chan8_raw;
         	/**
        * System ID
        */
        public byte target_system;
         	/**
        * Component ID
        */
        public byte target_component;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE;
        		packet.payload.putShort(chan1_raw);
        		packet.payload.putShort(chan2_raw);
        		packet.payload.putShort(chan3_raw);
        		packet.payload.putShort(chan4_raw);
        		packet.payload.putShort(chan5_raw);
        		packet.payload.putShort(chan6_raw);
        		packet.payload.putShort(chan7_raw);
        		packet.payload.putShort(chan8_raw);
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        
		return packet;
        }
        
        /**
        * Decode a rc_channels_override message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.chan1_raw = payload.getShort();
        	    this.chan2_raw = payload.getShort();
        	    this.chan3_raw = payload.getShort();
        	    this.chan4_raw = payload.getShort();
        	    this.chan5_raw = payload.getShort();
        	    this.chan6_raw = payload.getShort();
        	    this.chan7_raw = payload.getShort();
        	    this.chan8_raw = payload.getShort();
        	    this.target_system = payload.getByte();
        	    this.target_component = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_rc_channels_override(){
    	msgid = MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_rc_channels_override(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "RC_CHANNELS_OVERRIDE");
        //Log.d("MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE", toString());
        }
        
                            
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE -"+" chan1_raw:"+chan1_raw+" chan2_raw:"+chan2_raw+" chan3_raw:"+chan3_raw+" chan4_raw:"+chan4_raw+" chan5_raw:"+chan5_raw+" chan6_raw:"+chan6_raw+" chan7_raw:"+chan7_raw+" chan8_raw:"+chan8_raw+" target_system:"+target_system+" target_component:"+target_component+"";
        }
        }
        