        // MESSAGE MOUNT_CONFIGURE PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Message to configure a camera mount, directional antenna, etc.
        */
        public class msg_mount_configure extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_MOUNT_CONFIGURE = 156;
        public static final int MAVLINK_MSG_LENGTH = 6;
        private static final long serialVersionUID = MAVLINK_MSG_ID_MOUNT_CONFIGURE;
        
        
         	/**
        * System ID
        */
        public byte target_system;
         	/**
        * Component ID
        */
        public byte target_component;
         	/**
        * mount operating mode (see MAV_MOUNT_MODE enum)
        */
        public byte mount_mode;
         	/**
        * (1 = yes, 0 = no)
        */
        public byte stab_roll;
         	/**
        * (1 = yes, 0 = no)
        */
        public byte stab_pitch;
         	/**
        * (1 = yes, 0 = no)
        */
        public byte stab_yaw;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_MOUNT_CONFIGURE;
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        		packet.payload.putByte(mount_mode);
        		packet.payload.putByte(stab_roll);
        		packet.payload.putByte(stab_pitch);
        		packet.payload.putByte(stab_yaw);
        
		return packet;
        }
        
        /**
        * Decode a mount_configure message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.target_system = payload.getByte();
        	    this.target_component = payload.getByte();
        	    this.mount_mode = payload.getByte();
        	    this.stab_roll = payload.getByte();
        	    this.stab_pitch = payload.getByte();
        	    this.stab_yaw = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_mount_configure(){
    	msgid = MAVLINK_MSG_ID_MOUNT_CONFIGURE;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_mount_configure(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_MOUNT_CONFIGURE;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "MOUNT_CONFIGURE");
        //Log.d("MAVLINK_MSG_ID_MOUNT_CONFIGURE", toString());
        }
        
                    
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_MOUNT_CONFIGURE -"+" target_system:"+target_system+" target_component:"+target_component+" mount_mode:"+mount_mode+" stab_roll:"+stab_roll+" stab_pitch:"+stab_pitch+" stab_yaw:"+stab_yaw+"";
        }
        }
        