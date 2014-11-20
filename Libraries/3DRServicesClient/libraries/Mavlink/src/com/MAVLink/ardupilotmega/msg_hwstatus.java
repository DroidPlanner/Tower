        // MESSAGE HWSTATUS PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Status of key hardware
        */
        public class msg_hwstatus extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_HWSTATUS = 165;
        public static final int MAVLINK_MSG_LENGTH = 3;
        private static final long serialVersionUID = MAVLINK_MSG_ID_HWSTATUS;
        
        
         	/**
        * board voltage (mV)
        */
        public short Vcc;
         	/**
        * I2C error count
        */
        public byte I2Cerr;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_HWSTATUS;
        		packet.payload.putShort(Vcc);
        		packet.payload.putByte(I2Cerr);
        
		return packet;
        }
        
        /**
        * Decode a hwstatus message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.Vcc = payload.getShort();
        	    this.I2Cerr = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_hwstatus(){
    	msgid = MAVLINK_MSG_ID_HWSTATUS;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_hwstatus(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_HWSTATUS;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "HWSTATUS");
        //Log.d("MAVLINK_MSG_ID_HWSTATUS", toString());
        }
        
            
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_HWSTATUS -"+" Vcc:"+Vcc+" I2Cerr:"+I2Cerr+"";
        }
        }
        