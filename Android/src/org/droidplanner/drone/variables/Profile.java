package org.droidplanner.drone.variables;

import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneVariable;
import org.droidplanner.file.IO.VehicleProfile;

import android.view.View;

import com.MAVLink.Messages.enums.MAV_TYPE;


public class Profile extends DroneVariable {
    public static final String ARDU_PLANE = "ArduPlane";
    public static final String ARDU_COPTER = "ArduCopter";
    public static final String ARDU_ROVER = "ArduRover";

    private VehicleProfile profile;


    public Profile(Drone myDrone) {
        super(myDrone);
    }

    public VehicleProfile getProfile() {
        return profile;
    }

    /**
     * Get vehicle type
     *
     * @return vehicleType from MAV_TYPE of connected vehicle or
     * configured vehicleType if disconnected or unsupported
     */
    public String getVehicleType() {
        if (myDrone.MavClient.isConnected()) {
            // online: derive from connected vehicle type
            switch(myDrone.type.getType()) {
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
                    // unsupported - fall thru to offline condition
            }
        }

        // offline or unsupported - return configured vehicleType
        return myDrone.preferences.getVehicleType();
    }

    /*
     * Load vehclie profile for current vehicle type
     */
    public void load() {
        profile = myDrone.preferences.loadVehicleProfile(getVehicleType());
    }

    /*
     * Apply vehicle view profiles to view identified by resId
     * Reload - want the most recent profile each time this call is made
     */
    public void applyMissionViewProfile(View view, int resId) {
        load();

        if(profile != null)
            profile.applyMissionViewProfile(view, resId);
    }
}
