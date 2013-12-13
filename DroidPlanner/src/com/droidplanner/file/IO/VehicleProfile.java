package com.droidplanner.file.IO;

import android.view.View;
import android.widget.TextView;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VehicleProfile {
    private String parameterMetadataType;
    private Default default_ = new Default();

    private final List<MissionViewProfile> missionViewProfiles = new ArrayList<MissionViewProfile>();
    private final Map<Integer, MissionDialogProfile> profileMissionDialogs = new HashMap<Integer, MissionDialogProfile>();


    public String getParameterMetadataType() {
        return parameterMetadataType;
    }

    public Default getDefault() {
        return default_;
    }

    public void setDefault(Default default_) {
        this.default_ = default_;
    }

    public void applyMissionViewProfile(View view, int resId) {
        // apply global view customizations
        applyViewProfiles(view, missionViewProfiles);

        // apply dialog
        final MissionDialogProfile dialogProfile = profileMissionDialogs.get(resId);
        if(dialogProfile != null)
            applyViewProfiles(view, dialogProfile.getMissionViewProfiles());
    }

    private void applyViewProfiles(View view, List<MissionViewProfile> profiles) {
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


    public void setParameterMetadataType(String parameterMetadataType) {
        this.parameterMetadataType = parameterMetadataType;
    }

    public ViewProfileBuilder getViewProfileBuilder()
    {
        return new ViewProfileBuilder() {
            @Override
            public void addViewProfile(MissionViewProfile viewProfile) {
                if (viewProfile != null)
                    missionViewProfiles.add(viewProfile);
            }
        };
    }

    public ViewProfileBuilder addDialogProfile(MissionDialogProfile newDialogProfile) {
        if (newDialogProfile != null) {
            // return existing profile builder for resId, retain and use newDialogProfile if none
            MissionDialogProfile dialogProfile = profileMissionDialogs.get(newDialogProfile.getResId());
            if(dialogProfile == null)
                profileMissionDialogs.put(newDialogProfile.getResId(), dialogProfile = newDialogProfile);
            return dialogProfile.getViewProfileBuilder();

        } else {
            return null;
        }
    }

    public static interface ViewProfileBuilder {
        void addViewProfile(MissionViewProfile viewProfile);
    }

    public static class MissionDialogProfile {
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

        public ViewProfileBuilder getViewProfileBuilder()
        {
            return new ViewProfileBuilder() {
                @Override
                public void addViewProfile(MissionViewProfile viewProfile) {
                    if (viewProfile != null)
                        missionViewProfiles.add(viewProfile);
                }
            };
        }
    }

    public static class MissionViewProfile {
        private int resId;
        private String text;
        private int visibility;
        private String type;

        private double min;
        private double max;
        private double inc;

        public int getResId() {
            return resId;
        }

        public void setResId(int resId) {
            this.resId = resId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getVisibility() {
            return visibility;
        }

        public void setVisibility(int visibility) {
            this.visibility = visibility;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getMin() {
            return min;
        }

        public void setMin(double min) {
            this.min = min;
        }

        public double getMax() {
            return max;
        }

        public void setMax(double max) {
            this.max = max;
        }

        public double getInc() {
            return inc;
        }

        public void setInc(double inc) {
            this.inc = inc;
        }
    }

    public static class Default
    {
        private int wpNavSpeed;
        private int maxAltitude;

        public int getWpNavSpeed() {
            return wpNavSpeed;
        }

        public void setWpNavSpeed(int wpNavSpeed) {
            this.wpNavSpeed = wpNavSpeed;
        }

        public int getMaxAltitude() {
            return maxAltitude;
        }

        public void setMaxAltitude(int maxAltitude) {
            this.maxAltitude = maxAltitude;
        }
    }
}
