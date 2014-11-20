        // MESSAGE ATTITUDE_QUATERNION_COV PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * The attitude in the aeronautical frame (right-handed, Z-down, X-front, Y-right), expressed as quaternion. Quaternion order is w, x, y, z and a zero rotation would be expressed as (1 0 0 0).
        */
        public class msg_attitude_quaternion_cov extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_ATTITUDE_QUATERNION_COV = 61;
        public static final int MAVLINK_MSG_LENGTH = 68;
        private static final long serialVersionUID = MAVLINK_MSG_ID_ATTITUDE_QUATERNION_COV;
        
        
         	/**
        * Timestamp (milliseconds since system boot)
        */
        public int time_boot_ms;
         	/**
        * Quaternion components, w, x, y, z (1 0 0 0 is the null-rotation)
        */
        public float q[] = new float[4];
         	/**
        * Roll angular speed (rad/s)
        */
        public float rollspeed;
         	/**
        * Pitch angular speed (rad/s)
        */
        public float pitchspeed;
         	/**
        * Yaw angular speed (rad/s)
        */
        public float yawspeed;
         	/**
        * Attitude covariance
        */
        public float covariance[] = new float[9];
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_ATTITUDE_QUATERNION_COV;
        		packet.payload.putInt(time_boot_ms);
        		 for (int i = 0; i < q.length; i++) {
                    packet.payload.putFloat(q[i]);
                    }
        		packet.payload.putFloat(rollspeed);
        		packet.payload.putFloat(pitchspeed);
        		packet.payload.putFloat(yawspeed);
        		 for (int i = 0; i < covariance.length; i++) {
                    packet.payload.putFloat(covariance[i]);
                    }
        
		return packet;
        }
        
        /**
        * Decode a attitude_quaternion_cov message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_boot_ms = payload.getInt();
        	     for (int i = 0; i < this.q.length; i++) {
                    this.q[i] = payload.getFloat();
                    }
        	    this.rollspeed = payload.getFloat();
        	    this.pitchspeed = payload.getFloat();
        	    this.yawspeed = payload.getFloat();
        	     for (int i = 0; i < this.covariance.length; i++) {
                    this.covariance[i] = payload.getFloat();
                    }
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_attitude_quaternion_cov(){
    	msgid = MAVLINK_MSG_ID_ATTITUDE_QUATERNION_COV;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_attitude_quaternion_cov(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_ATTITUDE_QUATERNION_COV;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "ATTITUDE_QUATERNION_COV");
        //Log.d("MAVLINK_MSG_ID_ATTITUDE_QUATERNION_COV", toString());
        }
        
                    
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_ATTITUDE_QUATERNION_COV -"+" time_boot_ms:"+time_boot_ms+" q:"+q+" rollspeed:"+rollspeed+" pitchspeed:"+pitchspeed+" yawspeed:"+yawspeed+" covariance:"+covariance+"";
        }
        }
        