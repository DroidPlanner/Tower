        // MESSAGE COMMAND_ACK PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Report status of a command. Includes feedback wether the command was executed.
        */
        public class msg_command_ack extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_COMMAND_ACK = 77;
        public static final int MAVLINK_MSG_LENGTH = 3;
        private static final long serialVersionUID = MAVLINK_MSG_ID_COMMAND_ACK;
        
        
         	/**
        * Command ID, as defined by MAV_CMD enum.
        */
        public short command;
         	/**
        * See MAV_RESULT enum
        */
        public byte result;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_COMMAND_ACK;
        		packet.payload.putShort(command);
        		packet.payload.putByte(result);
        
		return packet;
        }
        
        /**
        * Decode a command_ack message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.command = payload.getShort();
        	    this.result = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_command_ack(){
    	msgid = MAVLINK_MSG_ID_COMMAND_ACK;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_command_ack(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_COMMAND_ACK;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "COMMAND_ACK");
        //Log.d("MAVLINK_MSG_ID_COMMAND_ACK", toString());
        }
        
            
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_COMMAND_ACK -"+" command:"+command+" result:"+result+"";
        }
        }
        