package com.droidplanner.drone.variables;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class Type extends DroneVariable {
    public static final String ARDU_PLANE = "ArduPlane";
    public static final String ARDU_COPTER = "ArduCopter";
    public static final String ARDU_ROVER = "ArduRover";

    private int type = MAV_TYPE.MAV_TYPE_FIXED_WING;

	public Type(Drone myDrone) {
		super(myDrone);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		if (this.type != type) {
			this.type = type;
            myDrone.profile.load();
			myDrone.notifyTypeChanged();
		}
	}

    /**
     * Get vehicle type
     *
     * @return vehicleType from MAV_TYPE of connected vehicle or
     * configured vehicleType if disconnected or not supported
     * null if not supported
     */
    public String getVehicleType() {
        if (myDrone.MavClient.isConnected()) {
            // online: derive from connected vehicle type
            switch(type) {
                case MAV_TYPE.MAV_TYPE_FIXED_WING: /* Fixed wing aircraft. | */
                    return ARDU_PLANE;

                case MAV_TYPE.MAV_TYPE_GENERIC: /* Generic micro air vehicle. | */
                case MAV_TYPE.MAV_TYPE_QUADROTOR: /* Quadrotor | */
                case MAV_TYPE.MAV_TYPE_COAXIAL: /* Coaxial helicopter | */
                case MAV_TYPE.MAV_TYPE_HELICOPTER: /* Normal helicopter with tail rotor. | */
                case MAV_TYPE.MAV_TYPE_HEXAROTOR: /* Hexarotor | */
                case MAV_TYPE.MAV_TYPE_OCTOROTOR: /* Octorotor | */
                case MAV_TYPE.MAV_TYPE_TRICOPTER: /* Octorotor | */
                    return ARDU_COPTER;

                case MAV_TYPE.MAV_TYPE_GROUND_ROVER: /* Ground rover | */
                case MAV_TYPE.MAV_TYPE_SURFACE_BOAT: /* Surface vessel, boat, ship | */
                    return ARDU_ROVER;

//              Unused but here for documention
//                case MAV_TYPE.MAV_TYPE_ANTENNA_TRACKER: /* Ground installation | */
//                case MAV_TYPE.MAV_TYPE_GCS: /* Operator control unit / ground control station | */
//                case MAV_TYPE.MAV_TYPE_AIRSHIP: /* Airship, controlled | */
//                case MAV_TYPE.MAV_TYPE_FREE_BALLOON: /* Free balloon, uncontrolled | */
//                case MAV_TYPE.MAV_TYPE_ROCKET: /* Rocket | */
//                case MAV_TYPE.MAV_TYPE_SUBMARINE: /* Submarine | */
//                case MAV_TYPE.MAV_TYPE_FLAPPING_WING: /* Flapping wing | */
//                case MAV_TYPE.MAV_TYPE_KITE: /* Flapping wing | */
                default:
                    // unsupported - fall thru to disconnected condition
            }
        }

        // offline or unsupported - return configured vehicleType
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(myDrone.context);
        return prefs.getString("pref_vehicle_type", null);
    }
}