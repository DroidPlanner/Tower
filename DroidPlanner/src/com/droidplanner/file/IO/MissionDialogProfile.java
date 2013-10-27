package com.droidplanner.file.IO;

import java.util.ArrayList;
import java.util.List;


public class MissionDialogProfile {
    private int resId;
    private List<MissionViewProfile> missionViewProfiles = new ArrayList<MissionViewProfile>();


    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public List<MissionViewProfile> getMissionViewProfiles() {
        return missionViewProfiles;
    }
}
