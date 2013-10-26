package com.droidplanner.file.IO;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.file.DirectoryPath;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class VehicleProfile {
    private static final String VEHICLEPROFILE_PATH = "VehicleProfiles";

    private String metadataType;
    private List<VehicleMissionParameter> missionParameters;


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

    public String getMetadataType() {
        return metadataType;
    }

    public void setMetadataType(String metadataType) {
        this.metadataType = metadataType;
    }

    public void setMissionParameters(List<VehicleMissionParameter> missionParameters) {
        this.missionParameters = missionParameters;
    }

    public void customizeView(View view) {
        for (VehicleMissionParameter missionParameter : missionParameters) {
            // find control view
            final View ctl = view.findViewById(missionParameter.getResId());
            if(ctl == null)
                continue;

            // set visibility
            ctl.setVisibility(missionParameter.getVisibility());

            if("SeekBarWithText".equals(missionParameter.getType()) && (ctl instanceof SeekBarWithText))
                ((SeekBarWithText) ctl).setMinMaxInc(missionParameter.getMin(), missionParameter.getMax(), missionParameter.getInc());
        }
    }
}
