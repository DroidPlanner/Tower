package com.droidplanner.activitys;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;
import com.droidplanner.widgets.graph.Chart;
import com.droidplanner.widgets.graph.ChartCheckBoxList;

public class ChartActivity extends SuperActivity implements HudUpdatedListner {

	public Chart chart;
	public LinearLayout readoutMenu;
	public String[] labels = { "Pitch", "Roll","Yaw", "Altitude", "Target Alt.", "Latitude",
			"Longitude","SatCount" ,"Voltage", "Current", "G. Speed", "A. Speed","WP","DistToWp", };
	public ChartCheckBoxList checkBoxList = new ChartCheckBoxList();

	@Override
	public int getNavigationItem() {
		return 6;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chart);

		chart = (Chart) findViewById(R.id.chart);
		readoutMenu = (LinearLayout) findViewById(R.id.readoutMenu);

		checkBoxList.populateView(readoutMenu, labels, chart);

		drone.setHudListner(this);
	}

	@Override
	public void onDroneUpdate() {
		checkBoxList.updateCheckBox("Pitch", drone.orientation.getPitch());
		checkBoxList.updateCheckBox("Roll", drone.orientation.getRoll());
		checkBoxList.updateCheckBox("Yaw", drone.orientation.getYaw());
		checkBoxList.updateCheckBox("Altitude", drone.altitude.getAltitude());
		checkBoxList.updateCheckBox("Target Alt.", drone.altitude.getTargetAltitude());
		checkBoxList.updateCheckBox("Latitude",	drone.GPS.getPosition().latitude);
		checkBoxList.updateCheckBox("Longitude", drone.GPS.getPosition().longitude);
		checkBoxList.updateCheckBox("SatCount", drone.GPS.getSatCount());
		checkBoxList.updateCheckBox("Voltage", drone.battery.getBattVolt());
		checkBoxList.updateCheckBox("Current", drone.battery.getBattCurrent());
		checkBoxList.updateCheckBox("G. Speed", drone.speed.getGroundSpeed());
		checkBoxList.updateCheckBox("A. Speed", drone.speed.getAirSpeed());
		checkBoxList.updateCheckBox("DistToWp", drone.mission.getDisttowp());
		checkBoxList.updateCheckBox("WP", drone.mission.getWpno());

		chart.update();
	}

}