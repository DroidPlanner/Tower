package com.droidplanner.drone.variables;

import android.content.res.AssetManager;
import android.view.View;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;
import com.droidplanner.file.DirectoryPath;
import com.droidplanner.file.IO.VehicleProfile;
import com.droidplanner.file.IO.VehicleProfileReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class Profile extends DroneVariable {
    private static final String VEHICLEPROFILE_PATH = "VehicleProfiles";
    private VehicleProfile profile;


    public Profile(Drone myDrone) {
        super(myDrone);
    }

    public VehicleProfile getProfile() {
        return profile;
    }

    /*
     * Load vehclie profile for current vehicle type
     */
    public void load() {
        final String fileName = myDrone.type.getVehicleType() + ".xml";
        final String path = VEHICLEPROFILE_PATH + File.separator + fileName;

        profile = null;
        try {
            final VehicleProfile newProfile = new VehicleProfile();

            // load profile from resources first
            final AssetManager assetManager = myDrone.context.getAssets();
            if(assetExists(assetManager, VEHICLEPROFILE_PATH, fileName)) {
                final InputStream inputStream = assetManager.open(path);
                VehicleProfileReader.open(inputStream, newProfile);
            }

            // load (override) from file if available
            final File file = new File(DirectoryPath.getDroidPlannerPath() + path);
            if(file.exists()) {
                final InputStream inputStream = new FileInputStream(file);
                VehicleProfileReader.open(inputStream, newProfile);
            }
            profile = newProfile;

        } catch (Exception e) {
            // nop
        }
    }

    private boolean assetExists(AssetManager assetManager, String directory, String fileName) throws IOException {
        final String[] assets = assetManager.list(directory);
        for (String asset : assets)
            if(asset.equals(fileName))
                return true;
        return false;
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
