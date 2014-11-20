        // MESSAGE DIGICAM_CONTROL PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Control on-board Camera Control System to take shots.
        */
        public class msg_digicam_control extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_DIGICAM_CONTROL = 155;
        public static final int MAVLINK_MSG_LENGTH = 13;
        private static final long serialVersionUID = MAVLINK_MSG_ID_DIGICAM_CONTROL;
        
        
         	/**
        * Correspondent value to given extra_param
        */
        public float extra_value;
         	/**
        * System ID
        */
        public byte target_system;
         	/**
        * Component ID
        */
        public byte target_component;
         	/**
        * 0: stop, 1: start or keep it up //Session control e.g. show/hide lens
        */
        public byte session;
         	/**
        * 1 to N //Zoom's absolute position (0 means ignore)
        */
        public byte zoom_pos;
         	/**
        * -100 to 100 //Zooming step value to offset zoom from the current position
        */
        public byte zoom_step;
         	/**
        * 0: unlock focus or keep unlocked, 1: lock focus or keep locked, 3: re-lock focus
        */
        public byte focus_lock;
         	/**
        * 0: ignore, 1: shot or start filming
        */
        public byte shot;
         	/**
        * Command Identity (incremental loop: 0 to 255)//A command sent multiple times will be executed or pooled just once
        */
        public byte command_id;
         	/**
        * Extra parameters enumeration (0 means ignore)
        */
        public byte extra_param;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_DIGICAM_CONTROL;
        		packet.payload.putFloat(extra_value);
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(target_component);
        		packet.payload.putByte(session);
        		packet.payload.putByte(zoom_pos);
        		packet.payload.putByte(zoom_step);
        		packet.payload.putByte(focus_lock);
        		packet.payload.putByte(shot);
        		packet.payload.putByte(command_id);
        		packet.payload.putByte(extra_param);
        
		return packet;
        }
        
        /**
        * Decode a digicam_control message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.extra_value = payload.getFloat();
        	    this.target_system = payload.getByte();
        	    this.target_component = payload.getByte();
        	    this.session = payload.getByte();
        	    this.zoom_pos = payload.getByte();
        	    this.zoom_step = payload.getByte();
        	    this.focus_lock = payload.getByte();
        	    this.shot = payload.getByte();
        	    this.command_id = payload.getByte();
        	    this.extra_param = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_digicam_control(){
    	msgid = MAVLINK_MSG_ID_DIGICAM_CONTROL;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_digicam_control(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_DIGICAM_CONTROL;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "DIGICAM_CONTROL");
        //Log.d("MAVLINK_MSG_ID_DIGICAM_CONTROL", toString());
        }
        
                            
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_DIGICAM_CONTROL -"+" extra_value:"+extra_value+" target_system:"+target_system+" target_component:"+target_component+" session:"+session+" zoom_pos:"+zoom_pos+" zoom_step:"+zoom_step+" focus_lock:"+focus_lock+" shot:"+shot+" command_id:"+command_id+" extra_param:"+extra_param+"";
        }
        }
        