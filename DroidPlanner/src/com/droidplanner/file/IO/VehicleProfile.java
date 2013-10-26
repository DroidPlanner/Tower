package com.droidplanner.file.IO;

import android.view.View;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;

import java.util.List;


public class VehicleProfile {
    private List<VehicleParameter> parameters;

    public List<VehicleParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<VehicleParameter> parameters) {
        this.parameters = parameters;
    }

    public void customizeView(View view) {
        for (VehicleParameter parameter : parameters) {
            // find control view
            final View ctl = view.findViewById(parameter.getResId());
            if(ctl == null)
                continue;

            // set visibility
            ctl.setVisibility(parameter.getVisibility());

            if("SeekBarWithText".equals(parameter.getType()) && (ctl instanceof SeekBarWithText))
                ((SeekBarWithText) ctl).setMinMaxInc(parameter.getMin(), parameter.getMax(), parameter.getInc());
        }
    }
}
