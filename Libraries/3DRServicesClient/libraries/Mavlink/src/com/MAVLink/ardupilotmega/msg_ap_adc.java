        // MESSAGE AP_ADC PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * raw ADC output
        */
        public class msg_ap_adc extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_AP_ADC = 153;
        public static final int MAVLINK_MSG_LENGTH = 12;
        private static final long serialVersionUID = MAVLINK_MSG_ID_AP_ADC;
        
        
         	/**
        * ADC output 1
        */
        public short adc1;
         	/**
        * ADC output 2
        */
        public short adc2;
         	/**
        * ADC output 3
        */
        public short adc3;
         	/**
        * ADC output 4
        */
        public short adc4;
         	/**
        * ADC output 5
        */
        public short adc5;
         	/**
        * ADC output 6
        */
        public short adc6;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_AP_ADC;
        		packet.payload.putShort(adc1);
        		packet.payload.putShort(adc2);
        		packet.payload.putShort(adc3);
        		packet.payload.putShort(adc4);
        		packet.payload.putShort(adc5);
        		packet.payload.putShort(adc6);
        
		return packet;
        }
        
        /**
        * Decode a ap_adc message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.adc1 = payload.getShort();
        	    this.adc2 = payload.getShort();
        	    this.adc3 = payload.getShort();
        	    this.adc4 = payload.getShort();
        	    this.adc5 = payload.getShort();
        	    this.adc6 = payload.getShort();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_ap_adc(){
    	msgid = MAVLINK_MSG_ID_AP_ADC;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_ap_adc(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_AP_ADC;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "AP_ADC");
        //Log.d("MAVLINK_MSG_ID_AP_ADC", toString());
        }
        
                    
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_AP_ADC -"+" adc1:"+adc1+" adc2:"+adc2+" adc3:"+adc3+" adc4:"+adc4+" adc5:"+adc5+" adc6:"+adc6+"";
        }
        }
        