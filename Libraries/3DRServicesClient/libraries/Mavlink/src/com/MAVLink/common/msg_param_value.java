        // MESSAGE PARAM_VALUE PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Emit the value of a onboard parameter. The inclusion of param_count and param_index in the message allows the recipient to keep track of received parameters and allows him to re-request missing parameters after a loss or timeout.
        */
        public class msg_param_value extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_PARAM_VALUE = 22;
        public static final int MAVLINK_MSG_LENGTH = 25;
        private static final long serialVersionUID = MAVLINK_MSG_ID_PARAM_VALUE;
        
        
         	/**
        * Onboard parameter value
        */
        public float param_value;
         	/**
        * Total number of onboard parameters
        */
        public short param_count;
         	/**
        * Index of this onboard parameter
        */
        public short param_index;
         	/**
        * Onboard parameter id, terminated by NULL if the length is less than 16 human-readable chars and WITHOUT null termination (NULL) byte if the length is exactly 16 chars - applications have to provide 16+1 bytes storage if the ID is stored as string
        */
        public byte param_id[] = new byte[16];
         	/**
        * Onboard parameter type: see the MAV_PARAM_TYPE enum for supported data types.
        */
        public byte param_type;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_PARAM_VALUE;
        		packet.payload.putFloat(param_value);
        		packet.payload.putShort(param_count);
        		packet.payload.putShort(param_index);
        		 for (int i = 0; i < param_id.length; i++) {
                    packet.payload.putByte(param_id[i]);
                    }
        		packet.payload.putByte(param_type);
        
		return packet;
        }
        
        /**
        * Decode a param_value message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.param_value = payload.getFloat();
        	    this.param_count = payload.getShort();
        	    this.param_index = payload.getShort();
        	     for (int i = 0; i < this.param_id.length; i++) {
                    this.param_id[i] = payload.getByte();
                    }
        	    this.param_type = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_param_value(){
    	msgid = MAVLINK_MSG_ID_PARAM_VALUE;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_param_value(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_PARAM_VALUE;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "PARAM_VALUE");
        //Log.d("MAVLINK_MSG_ID_PARAM_VALUE", toString());
        }
        
               /**
                        * Sets the buffer of this message with a string, adds the necessary padding
                        */
                        public void setParam_Id(String str) {
                        int len = Math.min(str.length(), 16);
                        for (int i=0; i<len; i++) {
                        param_id[i] = (byte) str.charAt(i);
                        }
                        for (int i=len; i<16; i++) {			// padding for the rest of the buffer
                        param_id[i] = 0;
                        }
                        }
                        
                        /**
                        * Gets the message, formated as a string
                        */
                        public String getParam_Id() {
                        String result = "";
                        for (int i = 0; i < 16; i++) {
                        if (param_id[i] != 0)
                        result = result + (char) param_id[i];
                        else
                        break;
                        }
                        return result;
                        
                        }   
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_PARAM_VALUE -"+" param_value:"+param_value+" param_count:"+param_count+" param_index:"+param_index+" param_id:"+param_id+" param_type:"+param_type+"";
        }
        }
        