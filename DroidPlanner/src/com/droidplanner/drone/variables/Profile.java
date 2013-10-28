package com.droidplanner.drone.variables;

import android.view.View;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;
import com.droidplanner.file.DirectoryPath;
import com.droidplanner.file.IO.VehicleProfile;
import com.droidplanner.file.IO.VehicleProfileReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


public class Profile extends DroneVariable {
    private static final String VEHICLEPROFILE_PATH = "VehicleProfiles";
    private VehicleProfile profile;


    public Profile(Drone myDrone) {
        super(myDrone);
    }

    public VehicleProfile getProfile() {
        load();

        return profile;
    }

    /*
     * Load vehclie profile for current vehicle type
     */
    public void load() {
        final String path = VEHICLEPROFILE_PATH + File.separator + myDrone.type.getVehicleType() + ".xml";
        try {
            final InputStream inputStream;
            final File file = new File(DirectoryPath.getDroidPlannerPath() + path);
            if(file.exists()) {
                inputStream = new FileInputStream(file);
            }
            else {
                inputStream = myDrone.context.getAssets().open(path);
            }
            profile = VehicleProfileReader.open(inputStream);
        } catch (Exception e) {
            profile = null;
        }
    }

    /*
     * Apply vehicle view profiles to view identified by resId
     */
    public void applyViewProfile(View view, int resId) {
        load();

        if(profile != null)
            profile.applyMissionViewProfile(view, resId);
    }
}
