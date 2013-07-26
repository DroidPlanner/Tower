package com.droidplanner.activitys;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;
import com.droidplanner.widgets.graph.Chart;
import com.droidplanner.widgets.graph.ChartCheckBox;

public class ChartActivity extends SuperActivity implements
		 HudUpdatedListner {

	private Chart chart;
	private LinearLayout readoutMenu;

	String[] labels = { "Pitch", "Yaw", "Roll" };

	private List<ChartCheckBox> CheckBoxList = new ArrayList<ChartCheckBox>();

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

		setupReadoutMenu();

		drone.setHudListner(this);
	}

	void setupReadoutMenu() {
		for (String label : labels) {
			ChartCheckBox checkBox = new ChartCheckBox(readoutMenu.getContext(),label, chart);
			CheckBoxList.add(checkBox);
			readoutMenu.addView(checkBox);
		}
	}


	@Override
	public void onDroneUpdate() {
		chart.series.get(0).newData(drone.orientation.getPitch());
		chart.series.get(1).newData(drone.orientation.getRoll());
		chart.series.get(2).newData(drone.orientation.getYaw());
		chart.update();

	}

}