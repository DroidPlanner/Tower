        // MESSAGE SYS_STATUS PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
        //import android.util.Log;
        
        /**
        * The general system state. If the system is following the MAVLink standard, the system state is mainly defined by three orthogonal states/modes: The system mode, which is either LOCKED (motors shut down and locked), MANUAL (system under RC control), GUIDED (system with autonomous position control, position setpoint controlled manually) or AUTO (system guided by path/waypoint planner). The NAV_MODE defined the current flight state: LIFTOFF (often an open-loop maneuver), LANDING, WAYPOINTS or VECTOR. This represents the internal navigation state machine. The system status shows wether the system is currently active or not and if an emergency occured. During the CRITICAL and EMERGENCY states the MAV is still considered to be active, but should start emergency procedures autonomously. After a failure occured it should first move from active to critical to allow manual intervention and then move to emergency after a certain timeout.
        */
        public class msg_sys_status extends MAVLinkMessage{
        
        public static final int MAVLINK_MSG_ID_SYS_STATUS = 1;
        public static final int MAVLINK_MSG_LENGTH = 31;
        private static final long serialVersionUID = MAVLINK_MSG_ID_SYS_STATUS;
        
        
         	/**
        * Bitmask showing which onboard controllers and sensors are present. Value of 0: not present. Value of 1: present. Indices defined by ENUM MAV_SYS_STATUS_SENSOR
        */
        public int onboard_control_sensors_present;
         	/**
        * Bitmask showing which onboard controllers and sensors are enabled:  Value of 0: not enabled. Value of 1: enabled. Indices defined by ENUM MAV_SYS_STATUS_SENSOR
        */
        public int onboard_control_sensors_enabled;
         	/**
        * Bitmask showing which onboard controllers and sensors are operational or have an error:  Value of 0: not enabled. Value of 1: enabled. Indices defined by ENUM MAV_SYS_STATUS_SENSOR
        */
        public int onboard_control_sensors_health;
         	/**
        * Maximum usage in percent of the mainloop time, (0%: 0, 100%: 1000) should be always below 1000
        */
        public short load;
         	/**
        * Battery voltage, in millivolts (1 = 1 millivolt)
        */
        public short voltage_battery;
         	/**
        * Battery current, in 10*milliamperes (1 = 10 milliampere), -1: autopilot does not measure the current
        */
        public short current_battery;
         	/**
        * Communication drops in percent, (0%: 0, 100%: 10'000), (UART, I2C, SPI, CAN), dropped packets on all links (packets that were corrupted on reception on the MAV)
        */
        public short drop_rate_comm;
         	/**
        * Communication errors (UART, I2C, SPI, CAN), dropped packets on all links (packets that were corrupted on reception on the MAV)
        */
        public short errors_comm;
         	/**
        * Autopilot-specific errors
        */
        public short errors_count1;
         	/**
        * Autopilot-specific errors
        */
        public short errors_count2;
         	/**
        * Autopilot-specific errors
        */
        public short errors_count3;
         	/**
        * Autopilot-specific errors
        */
        public short errors_count4;
         	/**
        * Remaining battery energy: (0%: 0, 100%: 100), -1: autopilot estimate the remaining battery
        */
        public byte battery_remaining;
        
        
        /**
        * Generates the payload for a mavlink message for a message of this type
        * @return
        */
        public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_SYS_STATUS;
        		packet.payload.putInt(onboard_control_sensors_present);
        		packet.payload.putInt(onboard_control_sensors_enabled);
        		packet.payload.putInt(onboard_control_sensors_health);
        		packet.payload.putShort(load);
        		packet.payload.putShort(voltage_battery);
        		packet.payload.putShort(current_battery);
        		packet.payload.putShort(drop_rate_comm);
        		packet.payload.putShort(errors_comm);
        		packet.payload.putShort(errors_count1);
        		packet.payload.putShort(errors_count2);
        		packet.payload.putShort(errors_count3);
        		packet.payload.putShort(errors_count4);
        		packet.payload.putByte(battery_remaining);
        
		return packet;
        }
        
        /**
        * Decode a sys_status message into this class fields
        *
        * @param payload The message to decode
        */
        public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        	    this.onboard_control_sensors_present = payload.getInt();
        	    this.onboard_control_sensors_enabled = payload.getInt();
        	    this.onboard_control_sensors_health = payload.getInt();
        	    this.load = payload.getShort();
        	    this.voltage_battery = payload.getShort();
        	    this.current_battery = payload.getShort();
        	    this.drop_rate_comm = payload.getShort();
        	    this.errors_comm = payload.getShort();
        	    this.errors_count1 = payload.getShort();
        	    this.errors_count2 = payload.getShort();
        	    this.errors_count3 = payload.getShort();
        	    this.errors_count4 = payload.getShort();
        	    this.battery_remaining = payload.getByte();
        
        }
        
        /**
        * Constructor for a new message, just initializes the msgid
        */
        public msg_sys_status(){
    	msgid = MAVLINK_MSG_ID_SYS_STATUS;
        }
        
        /**
        * Constructor for a new message, initializes the message with the payload
        * from a mavlink packet
        *
        */
        public msg_sys_status(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SYS_STATUS;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "SYS_STATUS");
        //Log.d("MAVLINK_MSG_ID_SYS_STATUS", toString());
        }
        
                                  
        /**
        * Returns a string with the MSG name and data
        */
        public String toString(){
    	return "MAVLINK_MSG_ID_SYS_STATUS -"+" onboard_control_sensors_present:"+onboard_control_sensors_present+" onboard_control_sensors_enabled:"+onboard_control_sensors_enabled+" onboard_control_sensors_health:"+onboard_control_sensors_health+" load:"+load+" voltage_battery:"+voltage_battery+" current_battery:"+current_battery+" drop_rate_comm:"+drop_rate_comm+" errors_comm:"+errors_comm+" errors_count1:"+errors_count1+" errors_count2:"+errors_count2+" errors_count3:"+errors_count3+" errors_count4:"+errors_count4+" battery_remaining:"+battery_remaining+"";
        }
        }
        