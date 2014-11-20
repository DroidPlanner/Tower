        // MESSAGE CAMERA_FEEDBACK PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Camera Capture Feedback
        */
        public class msg_camera_feedback extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_CAMERA_FEEDBACK = 180;
        public static final int MAVLINK_MSG_LENGTH = 45;
        private static final long serialVersionUID = MAVLINK_MSG_ID_CAMERA_FEEDBACK;
        
        
         	/**
        * Image timestamp (microseconds since UNIX epoch), as passed in by CAMERA_STATUS message (or autopilot if no CCB)
        */
        public long time_usec;
         	/**
        * Latitude in (deg * 1E7)
        */
        public int lat;
         	/**
        * Longitude in (deg * 1E7)
        */
        public int lng;
         	/**
        * Altitude Absolute (meters AMSL)
        */
        public float alt_msl;
         	/**
        * Altitude Relative (meters above HOME location)
        */
        public float alt_rel;
         	/**
        * Camera Roll angle (earth frame, degrees, +-180)
        */
        public float roll;
         	/**
        * Camera Pitch angle (earth frame, degrees, +-180)
        */
        public float pitch;
         	/**
        * Camera Yaw (earth frame, degrees, 0-360, true)
        */
        public float yaw;
         	/**
        * Focal Length (mm)
        */
        public float foc_len;
         	/**
        * Image index
        */
        public short img_idx;
         	/**
        * System ID
        */
        public byte target_system;
         	/**
        * Camera ID
        */
        public byte cam_idx;
         	/**
        * See CAMERA_FEEDBACK_FLAGS enum for definition of the bitmask
        */
        public byte flags;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_CAMERA_FEEDBACK;
        		packet.payload.putLong(time_usec);
        		packet.payload.putInt(lat);
        		packet.payload.putInt(lng);
        		packet.payload.putFloat(alt_msl);
        		packet.payload.putFloat(alt_rel);
        		packet.payload.putFloat(roll);
        		packet.payload.putFloat(pitch);
        		packet.payload.putFloat(yaw);
        		packet.payload.putFloat(foc_len);
        		packet.payload.putShort(img_idx);
        		packet.payload.putByte(target_system);
        		packet.payload.putByte(cam_idx);
        		packet.payload.putByte(flags);
        
		return packet;
        }
        
        /**
        * Decode a camera_feedback message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_usec = payload.getLong();
        	    this.lat = payload.getInt();
        	    this.lng = payload.getInt();
        	    this.alt_msl = payload.getFloat();
        	    this.alt_rel = payload.getFloat();
        	    this.roll = payload.getFloat();
        	    this.pitch = payload.getFloat();
        	    this.yaw = payload.getFloat();
        	    this.foc_len = payload.getFloat();
        	    this.img_idx = payload.getShort();
        	    this.target_system = payload.getByte();
        	    this.cam_idx = payload.getByte();
        	    this.flags = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_camera_feedback(){
    	msgid = MAVLINK_MSG_ID_CAMERA_FEEDBACK;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_camera_feedback(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_CAMERA_FEEDBACK;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "CAMERA_FEEDBACK");
        //Log.d("MAVLINK_MSG_ID_CAMERA_FEEDBACK", toString());
        }
        
                                  
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_CAMERA_FEEDBACK -"+" time_usec:"+time_usec+" lat:"+lat+" lng:"+lng+" alt_msl:"+alt_msl+" alt_rel:"+alt_rel+" roll:"+roll+" pitch:"+pitch+" yaw:"+yaw+" foc_len:"+foc_len+" img_idx:"+img_idx+" target_system:"+target_system+" cam_idx:"+cam_idx+" flags:"+flags+"";
        }
        }
        