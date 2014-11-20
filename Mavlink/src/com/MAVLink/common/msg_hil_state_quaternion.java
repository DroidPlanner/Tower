        // MESSAGE HIL_STATE_QUATERNION PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * Sent from simulation to autopilot, avoids in contrast to HIL_STATE singularities. This packet is useful for high throughput applications such as hardware in the loop simulations.
        */
        public class msg_hil_state_quaternion extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_HIL_STATE_QUATERNION = 115;
        public static final int MAVLINK_MSG_LENGTH = 64;
        private static final long serialVersionUID = MAVLINK_MSG_ID_HIL_STATE_QUATERNION;
        
        
         	/**
        * Timestamp (microseconds since UNIX epoch or microseconds since system boot)
        */
        public long time_usec;
         	/**
        * Vehicle attitude expressed as normalized quaternion in w, x, y, z order (with 1 0 0 0 being the null-rotation)
        */
        public float attitude_quaternion[] = new float[4];
         	/**
        * Body frame roll / phi angular speed (rad/s)
        */
        public float rollspeed;
         	/**
        * Body frame pitch / theta angular speed (rad/s)
        */
        public float pitchspeed;
         	/**
        * Body frame yaw / psi angular speed (rad/s)
        */
        public float yawspeed;
         	/**
        * Latitude, expressed as * 1E7
        */
        public int lat;
         	/**
        * Longitude, expressed as * 1E7
        */
        public int lon;
         	/**
        * Altitude in meters, expressed as * 1000 (millimeters)
        */
        public int alt;
         	/**
        * Ground X Speed (Latitude), expressed as m/s * 100
        */
        public short vx;
         	/**
        * Ground Y Speed (Longitude), expressed as m/s * 100
        */
        public short vy;
         	/**
        * Ground Z Speed (Altitude), expressed as m/s * 100
        */
        public short vz;
         	/**
        * Indicated airspeed, expressed as m/s * 100
        */
        public short ind_airspeed;
         	/**
        * True airspeed, expressed as m/s * 100
        */
        public short true_airspeed;
         	/**
        * X acceleration (mg)
        */
        public short xacc;
         	/**
        * Y acceleration (mg)
        */
        public short yacc;
         	/**
        * Z acceleration (mg)
        */
        public short zacc;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_HIL_STATE_QUATERNION;
        		packet.payload.putLong(time_usec);
        		 for (int i = 0; i < attitude_quaternion.length; i++) {
                    packet.payload.putFloat(attitude_quaternion[i]);
                    }
        		packet.payload.putFloat(rollspeed);
        		packet.payload.putFloat(pitchspeed);
        		packet.payload.putFloat(yawspeed);
        		packet.payload.putInt(lat);
        		packet.payload.putInt(lon);
        		packet.payload.putInt(alt);
        		packet.payload.putShort(vx);
        		packet.payload.putShort(vy);
        		packet.payload.putShort(vz);
        		packet.payload.putShort(ind_airspeed);
        		packet.payload.putShort(true_airspeed);
        		packet.payload.putShort(xacc);
        		packet.payload.putShort(yacc);
        		packet.payload.putShort(zacc);
        
		return packet;
        }
        
        /**
        * Decode a hil_state_quaternion message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.time_usec = payload.getLong();
        	     for (int i = 0; i < this.attitude_quaternion.length; i++) {
                    this.attitude_quaternion[i] = payload.getFloat();
                    }
        	    this.rollspeed = payload.getFloat();
        	    this.pitchspeed = payload.getFloat();
        	    this.yawspeed = payload.getFloat();
        	    this.lat = payload.getInt();
        	    this.lon = payload.getInt();
        	    this.alt = payload.getInt();
        	    this.vx = payload.getShort();
        	    this.vy = payload.getShort();
        	    this.vz = payload.getShort();
        	    this.ind_airspeed = payload.getShort();
        	    this.true_airspeed = payload.getShort();
        	    this.xacc = payload.getShort();
        	    this.yacc = payload.getShort();
        	    this.zacc = payload.getShort();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_hil_state_quaternion(){
    	msgid = MAVLINK_MSG_ID_HIL_STATE_QUATERNION;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_hil_state_quaternion(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_HIL_STATE_QUATERNION;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "HIL_STATE_QUATERNION");
        //Log.d("MAVLINK_MSG_ID_HIL_STATE_QUATERNION", toString());
        }
        
                                        
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_HIL_STATE_QUATERNION -"+" time_usec:"+time_usec+" attitude_quaternion:"+attitude_quaternion+" rollspeed:"+rollspeed+" pitchspeed:"+pitchspeed+" yawspeed:"+yawspeed+" lat:"+lat+" lon:"+lon+" alt:"+alt+" vx:"+vx+" vy:"+vy+" vz:"+vz+" ind_airspeed:"+ind_airspeed+" true_airspeed:"+true_airspeed+" xacc:"+xacc+" yacc:"+yacc+" zacc:"+zacc+"";
        }
        }
        