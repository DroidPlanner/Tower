package com.droidplanner.glass.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces;
import com.droidplanner.widgets.graph.Chart;
import com.droidplanner.widgets.graph.ChartCheckBoxList;

/**
 * @author Fredia Huya-Kouadio
 */
public class ChartFragment extends Fragment implements DroneInterfaces.HudUpdatedListner {

    private Drone drone;

    public Chart chart;
    public LinearLayout readoutMenu;
    public String[] labels = {"Pitch", "Roll", "Yaw", "Altitude", "Target Alt.", "Latitude",
            "Longitude", "SatCount", "Voltage", "Current", "G. Speed", "A. Speed"};
    public ChartCheckBoxList checkBoxList = new ChartCheckBoxList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_glass_chart, container, false);

        chart = (Chart) view.findViewById(R.id.chart);
        readoutMenu = (LinearLayout) view.findViewById(R.id.readoutMenu);

        checkBoxList.populateView(readoutMenu, labels, chart);
        readoutMenu.requestFocus();

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        drone = ((SuperActivity) getActivity()).drone;
        drone.setHudListner(this);
    }

    private void updateChart() {
        checkBoxList.updateCheckBox("Pitch", drone.orientation.getPitch());
        checkBoxList.updateCheckBox("Roll", drone.orientation.getRoll());
        checkBoxList.updateCheckBox("Yaw", drone.orientation.getYaw());
        checkBoxList.updateCheckBox("Altitude", drone.altitude.getAltitude());
        checkBoxList.updateCheckBox("Target Alt.", drone.altitude.getTargetAltitude());
        checkBoxList.updateCheckBox("Latitude", drone.GPS.getPosition().latitude);
        checkBoxList.updateCheckBox("Longitude", drone.GPS.getPosition().longitude);
        checkBoxList.updateCheckBox("SatCount", drone.GPS.getSatCount());
        checkBoxList.updateCheckBox("Voltage", drone.battery.getBattVolt());
        checkBoxList.updateCheckBox("Current", drone.battery.getBattCurrent());
        checkBoxList.updateCheckBox("G. Speed", drone.speed.getGroundSpeed());
        checkBoxList.updateCheckBox("A. Speed", drone.speed.getAirSpeed());

        chart.update();
    }

    @Override
    public void onOrientationUpdate() {
        updateChart();
    }

    @Override
    public void onSpeedAltitudeAndClimbRateUpdate() {
        updateChart();
    }
}