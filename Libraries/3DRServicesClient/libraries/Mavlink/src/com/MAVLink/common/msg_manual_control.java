        // MESSAGE MANUAL_CONTROL PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * This message provides an API for manually controlling the vehicle using standard joystick axes nomenclature, along with a joystick-like input device. Unused axes can be disabled an buttons are also transmit as boolean values of their 
        */
        public class msg_manual_control extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_MANUAL_CONTROL = 69;
        public static final int MAVLINK_MSG_LENGTH = 11;
        private static final long serialVersionUID = MAVLINK_MSG_ID_MANUAL_CONTROL;
        
        
         	/**
        * X-axis, normalized to the range [-1000,1000]. A value of INT16_MAX indicates that this axis is invalid. Generally corresponds to forward(1000)-backward(-1000) movement on a joystick and the pitch of a vehicle.
        */
        public short x;
         	/**
        * Y-axis, normalized to the range [-1000,1000]. A value of INT16_MAX indicates that this axis is invalid. Generally corresponds to left(-1000)-right(1000) movement on a joystick and the roll of a vehicle.
        */
        public short y;
         	/**
        * Z-axis, normalized to the range [-1000,1000]. A value of INT16_MAX indicates that this axis is invalid. Generally corresponds to a separate slider movement with maximum being 1000 and minimum being -1000 on a joystick and the thrust of a vehicle.
        */
        public short z;
         	/**
        * R-axis, normalized to the range [-1000,1000]. A value of INT16_MAX indicates that this axis is invalid. Generally corresponds to a twisting of the joystick, with counter-clockwise being 1000 and clockwise being -1000, and the yaw of a vehicle.
        */
        public short r;
         	/**
        * A bitfield corresponding to the joystick buttons' current state, 1 for pressed, 0 for released. The lowest bit corresponds to Button 1.
        */
        public short buttons;
         	/**
        * The system to be controlled.
        */
        public byte target;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_MANUAL_CONTROL;
        		packet.payload.putShort(x);
        		packet.payload.putShort(y);
        		packet.payload.putShort(z);
        		packet.payload.putShort(r);
        		packet.payload.putShort(buttons);
        		packet.payload.putByte(target);
        
		return packet;
        }
        
        /**
        * Decode a manual_control message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.x = payload.getShort();
        	    this.y = payload.getShort();
        	    this.z = payload.getShort();
        	    this.r = payload.getShort();
        	    this.buttons = payload.getShort();
        	    this.target = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_manual_control(){
    	msgid = MAVLINK_MSG_ID_MANUAL_CONTROL;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_manual_control(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_MANUAL_CONTROL;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "MANUAL_CONTROL");
        //Log.d("MAVLINK_MSG_ID_MANUAL_CONTROL", toString());
        }
        
                    
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_MANUAL_CONTROL -"+" x:"+x+" y:"+y+" z:"+z+" r:"+r+" buttons:"+buttons+" target:"+target+"";
        }
        }
        