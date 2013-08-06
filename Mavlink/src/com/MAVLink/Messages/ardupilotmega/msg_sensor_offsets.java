// MESSAGE SENSOR_OFFSETS PACKING
package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.MAVLinkPacket;
//import android.util.Log;

/**
* Offsets and calibrations values for hardware
        sensors. This makes it easier to debug the calibration process.
*/
public class msg_sensor_offsets extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_SENSOR_OFFSETS = 150;
	public static final int MAVLINK_MSG_LENGTH = 42;
	private static final long serialVersionUID = MAVLINK_MSG_ID_SENSOR_OFFSETS;
	

 	/**
	* magnetic declination (radians)
	*/
	public float mag_declination; 
 	/**
	* raw pressure from barometer
	*/
	public int raw_press; 
 	/**
	* raw temperature from barometer
	*/
	public int raw_temp; 
 	/**
	* gyro X calibration
	*/
	public float gyro_cal_x; 
 	/**
	* gyro Y calibration
	*/
	public float gyro_cal_y; 
 	/**
	* gyro Z calibration
	*/
	public float gyro_cal_z; 
 	/**
	* accel X calibration
	*/
	public float accel_cal_x; 
 	/**
	* accel Y calibration
	*/
	public float accel_cal_y; 
 	/**
	* accel Z calibration
	*/
	public float accel_cal_z; 
 	/**
	* magnetometer X offset
	*/
	public short mag_ofs_x; 
 	/**
	* magnetometer Y offset
	*/
	public short mag_ofs_y; 
 	/**
	* magnetometer Z offset
	*/
	public short mag_ofs_z; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_SENSOR_OFFSETS;
		packet.payload.putFloat(mag_declination);
		packet.payload.putInt(raw_press);
		packet.payload.putInt(raw_temp);
		packet.payload.putFloat(gyro_cal_x);
		packet.payload.putFloat(gyro_cal_y);
		packet.payload.putFloat(gyro_cal_z);
		packet.payload.putFloat(accel_cal_x);
		packet.payload.putFloat(accel_cal_y);
		packet.payload.putFloat(accel_cal_z);
		packet.payload.putShort(mag_ofs_x);
		packet.payload.putShort(mag_ofs_y);
		packet.payload.putShort(mag_ofs_z);
		return packet;		
	}

    /**
     * Decode a sensor_offsets message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
	    mag_declination = payload.getFloat();
	    raw_press = payload.getInt();
	    raw_temp = payload.getInt();
	    gyro_cal_x = payload.getFloat();
	    gyro_cal_y = payload.getFloat();
	    gyro_cal_z = payload.getFloat();
	    accel_cal_x = payload.getFloat();
	    accel_cal_y = payload.getFloat();
	    accel_cal_z = payload.getFloat();
	    mag_ofs_x = payload.getShort();
	    mag_ofs_y = payload.getShort();
	    mag_ofs_z = payload.getShort();    
    }

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_sensor_offsets(){
    	msgid = MAVLINK_MSG_ID_SENSOR_OFFSETS;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     * 
     */
    public msg_sensor_offsets(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_SENSOR_OFFSETS;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "SENSOR_OFFSETS");
        //Log.d("MAVLINK_MSG_ID_SENSOR_OFFSETS", toString());
    }
    
                        
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_SENSOR_OFFSETS -"+" mag_declination:"+mag_declination+" raw_press:"+raw_press+" raw_temp:"+raw_temp+" gyro_cal_x:"+gyro_cal_x+" gyro_cal_y:"+gyro_cal_y+" gyro_cal_z:"+gyro_cal_z+" accel_cal_x:"+accel_cal_x+" accel_cal_y:"+accel_cal_y+" accel_cal_z:"+accel_cal_z+" mag_ofs_x:"+mag_ofs_x+" mag_ofs_y:"+mag_ofs_y+" mag_ofs_z:"+mag_ofs_z+"";
    }
}
