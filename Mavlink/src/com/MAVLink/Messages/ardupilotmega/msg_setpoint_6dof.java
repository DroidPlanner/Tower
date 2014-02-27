// MESSAGE SETPOINT_6DOF PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
//import android.util.Log;

/**
* Set the 6 DOF setpoint for a attitude and position controller.
*/
public class msg_setpoint_6dof extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_SETPOINT_6DOF = 149;
	public static final int MAVLINK_MSG_LENGTH = 25;
	private static final long serialVersionUID = MAVLINK_MSG_ID_SETPOINT_6DOF;
	

 	/**
	* Translational Component in x
	*/
	public float trans_x; 
 	/**
	* Translational Component in y
	*/
	public float trans_y; 
 	/**
	* Translational Component in z
	*/
	public float trans_z; 
 	/**
	* Rotational Component in x
	*/
	public float rot_x; 
 	/**
	* Rotational Component in y
	*/
	public float rot_y; 
 	/**
	* Rotational Component in z
	*/
	public float rot_z; 
 	/**
	* System ID
	*/
	public byte target_system; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_SETPOINT_6DOF;
		packet.payload.putFloat(trans_x);
		packet.payload.putFloat(trans_y);
		packet.payload.putFloat(trans_z);
		packet.payload.putFloat(rot_x);
		packet.payload.putFloat(rot_y);
		packet.payload.putFloat(rot_z);
		packet.payload.putByte(target_system);
		return packet;		
	}

    /**
     * Decode a setpoint_6dof message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    trans_x = payload.getFloat();
	    trans_y = payload.getFloat();
	    trans_z = payload.getFloat();
	    rot_x = payload.getFloat();
	    rot_y = payload.getFloat();
	    rot_z = payload.getFloat();
	    target_system = payload.getByte();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_setpoint_6dof(){
    	msgid = MAVLINK_MSG_ID_SETPOINT_6DOF;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_setpoint_6dof(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SETPOINT_6DOF;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "SETPOINT_6DOF");
        //Log.d("MAVLINK_MSG_ID_SETPOINT_6DOF", toString());
    }
    
              
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_SETPOINT_6DOF -"+" trans_x:"+trans_x+" trans_y:"+trans_y+" trans_z:"+trans_z+" rot_x:"+rot_x+" rot_y:"+rot_y+" rot_z:"+rot_z+" target_system:"+target_system+"";
    }
}
