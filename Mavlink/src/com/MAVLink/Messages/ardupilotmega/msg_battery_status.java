// MESSAGE BATTERY_STATUS PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Transmitte battery informations for a accu pack.
*/
public class msg_battery_status extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_BATTERY_STATUS = 147;
	public static final int MAVLINK_MSG_LENGTH = 24;
	private static final long serialVersionUID = MAVLINK_MSG_ID_BATTERY_STATUS;
	

 	/**
	* Consumed charge, in milliampere hours (1 = 1 mAh), -1: autopilot does not provide mAh consumption estimate
	*/
	public int current_consumed; 
 	/**
	* Consumed energy, in 100*Joules (intergrated U*I*dt)  (1 = 100 Joule), -1: autopilot does not provide energy consumption estimate
	*/
	public int energy_consumed; 
 	/**
	* Battery voltage of cell 1, in millivolts (1 = 1 millivolt)
	*/
	public short voltage_cell_1; 
 	/**
	* Battery voltage of cell 2, in millivolts (1 = 1 millivolt), -1: no cell
	*/
	public short voltage_cell_2; 
 	/**
	* Battery voltage of cell 3, in millivolts (1 = 1 millivolt), -1: no cell
	*/
	public short voltage_cell_3; 
 	/**
	* Battery voltage of cell 4, in millivolts (1 = 1 millivolt), -1: no cell
	*/
	public short voltage_cell_4; 
 	/**
	* Battery voltage of cell 5, in millivolts (1 = 1 millivolt), -1: no cell
	*/
	public short voltage_cell_5; 
 	/**
	* Battery voltage of cell 6, in millivolts (1 = 1 millivolt), -1: no cell
	*/
	public short voltage_cell_6; 
 	/**
	* Battery current, in 10*milliamperes (1 = 10 milliampere), -1: autopilot does not measure the current
	*/
	public short current_battery; 
 	/**
	* Accupack ID
	*/
	public byte accu_id; 
 	/**
	* Remaining battery energy: (0%: 0, 100%: 100), -1: autopilot does not estimate the remaining battery
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
		packet.msgid = MAVLINK_MSG_ID_BATTERY_STATUS;
		packet.payload.putInt(current_consumed);
		packet.payload.putInt(energy_consumed);
		packet.payload.putShort(voltage_cell_1);
		packet.payload.putShort(voltage_cell_2);
		packet.payload.putShort(voltage_cell_3);
		packet.payload.putShort(voltage_cell_4);
		packet.payload.putShort(voltage_cell_5);
		packet.payload.putShort(voltage_cell_6);
		packet.payload.putShort(current_battery);
		packet.payload.putByte(accu_id);
		packet.payload.putByte(battery_remaining);
		return packet;		
	}

    /**
     * Decode a battery_status message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    current_consumed = payload.getInt();
	    energy_consumed = payload.getInt();
	    voltage_cell_1 = payload.getShort();
	    voltage_cell_2 = payload.getShort();
	    voltage_cell_3 = payload.getShort();
	    voltage_cell_4 = payload.getShort();
	    voltage_cell_5 = payload.getShort();
	    voltage_cell_6 = payload.getShort();
	    current_battery = payload.getShort();
	    accu_id = payload.getByte();
	    battery_remaining = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_battery_status(){
    	msgid = MAVLINK_MSG_ID_BATTERY_STATUS;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_battery_status(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_BATTERY_STATUS;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "BATTERY_STATUS");
        //Log.d("MAVLINK_MSG_ID_BATTERY_STATUS", toString());
    }
    
                      
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_BATTERY_STATUS -"+" current_consumed:"+current_consumed+" energy_consumed:"+energy_consumed+" voltage_cell_1:"+voltage_cell_1+" voltage_cell_2:"+voltage_cell_2+" voltage_cell_3:"+voltage_cell_3+" voltage_cell_4:"+voltage_cell_4+" voltage_cell_5:"+voltage_cell_5+" voltage_cell_6:"+voltage_cell_6+" current_battery:"+current_battery+" accu_id:"+accu_id+" battery_remaining:"+battery_remaining+"";
    }
}
