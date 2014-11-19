        // MESSAGE POWER_STATUS PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Power supply status
        */
        public class msg_power_status extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_POWER_STATUS = 125;
        public static final int MAVLINK_MSG_LENGTH = 6;
        private static final long serialVersionUID = MAVLINK_MSG_ID_POWER_STATUS;
        
        
         	/**
        * 5V rail voltage in millivolts
        */
        public short Vcc;
         	/**
        * servo rail voltage in millivolts
        */
        public short Vservo;
         	/**
        * power supply status flags (see MAV_POWER_STATUS enum)
        */
        public short flags;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_POWER_STATUS;
        		packet.payload.putShort(Vcc);
        		packet.payload.putShort(Vservo);
        		packet.payload.putShort(flags);
        
		return packet;
        }
        
        /**
        * Decode a power_status message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.Vcc = payload.getShort();
        	    this.Vservo = payload.getShort();
        	    this.flags = payload.getShort();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_power_status(){
    	msgid = MAVLINK_MSG_ID_POWER_STATUS;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_power_status(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_POWER_STATUS;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "POWER_STATUS");
        //Log.d("MAVLINK_MSG_ID_POWER_STATUS", toString());
        }
        
              
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_POWER_STATUS -"+" Vcc:"+Vcc+" Vservo:"+Vservo+" flags:"+flags+"";
        }
        }
        