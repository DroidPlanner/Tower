        // MESSAGE POSITION_TARGET_LOCAL_NED PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Set vehicle position, velocity and acceleration setpoint in local frame.
        */
        public class msg_position_target_local_ned extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_POSITION_TARGET_LOCAL_NED = 85;
        public static final int MAVLINK_MSG_LENGTH = 43;
        private static final long serialVersionUID = MAVLINK_MSG_ID_POSITION_TARGET_LOCAL_NED;
        
        
         	/**
        * Timestamp in milliseconds since system boot
        */
        public int time_boot_ms;
         	/**
        * X Position in NED frame in meters
        */
        public float x;
         	/**
        * Y Position in NED frame in meters
        */
        public float y;
         	/**
        * Z Position in NED frame in meters (note, altitude is negative in NED)
        */
        public float z;
         	/**
        * X velocity in NED frame in meter / s
        */
        public float vx;
         	/**
        * Y velocity in NED frame in meter / s
        */
        public float vy;
         	/**
        * Z velocity in NED frame in meter / s
        */
        public float vz;
         	/**
        * X acceleration or force (if bit 10 of type_mask is set) in NED frame in meter / s^2 or N
        */
        public float afx;
         	/**
        * Y acceleration or force (if bit 10 of type_mask is set) in NED frame in meter / s^2 or N
        */
        public float afy;
         	/**
        * Z acceleration or force (if bit 10 of type_mask is set) in NED frame in meter / s^2 or N
        */
        public float afz;
         	/**
        * Bitmask to indicate which dimensions should be ignored by the vehicle: a value of 0b0000000000000000 or 0b0000001000000000 indicates that none of the setpoint dimensions should be ignored. If bit 10 is set the floats afx afy afz should be interpreted as force instead of acceleration. Mapping: bit 1: x, bit 2: y, bit 3: z, bit 4: vx, bit 5: vy, bit 6: vz, bit 7: ax, bit 8: ay, bit 9: az, bit 10: is force setpoint
        */
        public short type_mask;
         	/**
        * Valid options are: MAV_FRAME_LOCAL_NED = 1, MAV_FRAME_LOCAL_OFFSET_NED = 7, MAV_FRAME_BODY_NED = 8, MAV_FRAME_BODY_OFFSET_NED = 9
        */
        public byte coordinate_frame;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_POSITION_TARGET_LOCAL_NED;
        		packet.payload.putInt(time_boot_ms);
        		packet.payload.putFloat(x);
        		packet.payload.putFloat(y);
        		packet.payload.putFloat(z);
        		packet.payload.putFloat(vx);
        		packet.payload.putFloat(vy);
        		packet.payload.putFloat(vz);
        		packet.payload.putFloat(afx);
        		packet.payload.putFloat(afy);
        		packet.payload.putFloat(afz);
        		packet.payload.putShort(type_mask);
        		packet.payload.putByte(coordinate_frame);
        
		return packet;
        }
        
        /**
        * Decode a position_target_local_ned message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_boot_ms = payload.getInt();
        	    this.x = payload.getFloat();
        	    this.y = payload.getFloat();
        	    this.z = payload.getFloat();
        	    this.vx = payload.getFloat();
        	    this.vy = payload.getFloat();
        	    this.vz = payload.getFloat();
        	    this.afx = payload.getFloat();
        	    this.afy = payload.getFloat();
        	    this.afz = payload.getFloat();
        	    this.type_mask = payload.getShort();
        	    this.coordinate_frame = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_position_target_local_ned(){
    	msgid = MAVLINK_MSG_ID_POSITION_TARGET_LOCAL_NED;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_position_target_local_ned(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_POSITION_TARGET_LOCAL_NED;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "POSITION_TARGET_LOCAL_NED");
        //Log.d("MAVLINK_MSG_ID_POSITION_TARGET_LOCAL_NED", toString());
        }
        
                                
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_POSITION_TARGET_LOCAL_NED -"+" time_boot_ms:"+time_boot_ms+" x:"+x+" y:"+y+" z:"+z+" vx:"+vx+" vy:"+vy+" vz:"+vz+" afx:"+afx+" afy:"+afy+" afz:"+afz+" type_mask:"+type_mask+" coordinate_frame:"+coordinate_frame+"";
        }
        }
        