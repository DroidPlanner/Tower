        // MESSAGE HIL_RC_INPUTS_RAW PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Sent from simulation to autopilot. The RAW values of the RC channels received. The standard PPM modulation is as follows: 1000 microseconds: 0%, 2000 microseconds: 100%. Individual receivers/transmitters might violate this specification.
        */
        public class msg_hil_rc_inputs_raw extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW = 92;
        public static final int MAVLINK_MSG_LENGTH = 33;
        private static final long serialVersionUID = MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW;
        
        
         	/**
        * Timestamp (microseconds since UNIX epoch or microseconds since system boot)
        */
        public long time_usec;
         	/**
        * RC channel 1 value, in microseconds
        */
        public short chan1_raw;
         	/**
        * RC channel 2 value, in microseconds
        */
        public short chan2_raw;
         	/**
        * RC channel 3 value, in microseconds
        */
        public short chan3_raw;
         	/**
        * RC channel 4 value, in microseconds
        */
        public short chan4_raw;
         	/**
        * RC channel 5 value, in microseconds
        */
        public short chan5_raw;
         	/**
        * RC channel 6 value, in microseconds
        */
        public short chan6_raw;
         	/**
        * RC channel 7 value, in microseconds
        */
        public short chan7_raw;
         	/**
        * RC channel 8 value, in microseconds
        */
        public short chan8_raw;
         	/**
        * RC channel 9 value, in microseconds
        */
        public short chan9_raw;
         	/**
        * RC channel 10 value, in microseconds
        */
        public short chan10_raw;
         	/**
        * RC channel 11 value, in microseconds
        */
        public short chan11_raw;
         	/**
        * RC channel 12 value, in microseconds
        */
        public short chan12_raw;
         	/**
        * Receive signal strength indicator, 0: 0%, 255: 100%
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
		packet.msgid = MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW;
        		packet.payload.putLong(time_usec);
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
        		packet.payload.putByte(rssi);
        
		return packet;
        }
        
        /**
        * Decode a hil_rc_inputs_raw message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_usec = payload.getLong();
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
        	    this.rssi = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_hil_rc_inputs_raw(){
    	msgid = MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_hil_rc_inputs_raw(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "HIL_RC_INPUTS_RAW");
        //Log.d("MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW", toString());
        }
        
                                    
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW -"+" time_usec:"+time_usec+" chan1_raw:"+chan1_raw+" chan2_raw:"+chan2_raw+" chan3_raw:"+chan3_raw+" chan4_raw:"+chan4_raw+" chan5_raw:"+chan5_raw+" chan6_raw:"+chan6_raw+" chan7_raw:"+chan7_raw+" chan8_raw:"+chan8_raw+" chan9_raw:"+chan9_raw+" chan10_raw:"+chan10_raw+" chan11_raw:"+chan11_raw+" chan12_raw:"+chan12_raw+" rssi:"+rssi+"";
        }
        }
        