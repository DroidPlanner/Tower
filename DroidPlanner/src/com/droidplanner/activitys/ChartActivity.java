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
	public String[] labels = { "Pitch", "Yaw", "Roll" };
	public ChartCheckBoxList checkBoxList = new ChartCheckBoxList();

	@Override
	public int getNavigationItem() {
		return 6;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chart);

		chart = (Chart) findViewById(R.id.scope);
		readoutMenu = (LinearLayout) findViewById(R.id.readoutMenu);

		checkBoxList.populateView(readoutMenu, labels, chart);

		drone.setHudListner(this);
	}

	@Override
	public void onDroneUpdate() {
		checkBoxList.updateCheckBox("Pitch", drone.orientation.getPitch());
		checkBoxList.updateCheckBox("Roll", drone.orientation.getRoll());
		checkBoxList.updateCheckBox("Yaw", drone.orientation.getYaw());
		chart.update();
	}

}