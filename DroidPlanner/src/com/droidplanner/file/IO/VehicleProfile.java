package com.droidplanner.file.IO;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.droidplanner.file.DirectoryPath;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VehicleProfile {
    private static final String VEHICLEPROFILE_PATH = "VehicleProfiles";

    private String parameterMetadataType;
    private final List<MissionViewProfile> missionViewProfiles = new ArrayList<MissionViewProfile>();
    private final Map<Integer, MissionDialogProfile> profileMissionDialogs = new HashMap<Integer, MissionDialogProfile>();


    /**
     * Load profile <vehicleType>.xml from 'VehicleProfiles' directory or
     * from resources if file not found
     */
    public static VehicleProfile load(Context context, String vehicleType) {
        final String path = VEHICLEPROFILE_PATH + File.separator + vehicleType + ".xml";

        try {
            final InputStream inputStream;
            final File file = new File(DirectoryPath.getDroidPlannerPath() + path);
            if(file.exists()) {
                // load from file
                inputStream = new FileInputStream(file);
            }
            else {
                // load from resource
                inputStream = context.getAssets().open(path);
            }
            // load
            return VehicleProfileReader.open(inputStream);
        } catch (Exception e) {
            // can't load
            return null;
        }
    }


    public String getParameterMetadataType() {
        return parameterMetadataType;
    }

    public void setParameterMetadataType(String parameterMetadataType) {
        this.parameterMetadataType = parameterMetadataType;
    }

    public List<MissionViewProfile> getMissionViewProfiles() {
        return missionViewProfiles;
    }

    public Map<Integer, MissionDialogProfile> getProfileMissionDialogs() {
        return profileMissionDialogs;
    }

    public void applyMissionDialogProfile(View dialog, int resId) {
        // apply global view customizations
        applyViewProfile(dialog, missionViewProfiles);

        // apply dialog
        final MissionDialogProfile dialogProfile = profileMissionDialogs.get(resId);
        if(dialogProfile != null)
            applyViewProfile(dialog, dialogProfile.getMissionViewProfiles());
    }


    private void applyViewProfile(View view, List<MissionViewProfile> profiles) {
        for (MissionViewProfile profile : profiles) {
            // find control view
            final View ctl = view.findViewById(profile.getResId());
            if(ctl == null)
                continue;

            // set text
            final String text = profile.getText();
            if(text != null && ctl instanceof TextView)
                ((TextView) ctl).setText(text);

            // set visibility
            ctl.setVisibility(profile.getVisibility());

            // set named control types
            if("SeekBarWithText".equals(profile.getType()) && (ctl instanceof SeekBarWithText))
                ((SeekBarWithText) ctl).setMinMaxInc(profile.getMin(), profile.getMax(), profile.getInc());
        }
    }
}
