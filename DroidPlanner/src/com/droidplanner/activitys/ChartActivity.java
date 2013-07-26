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
import com.droidplanner.widgets.graph.ChartSeries;

public class ChartActivity extends SuperActivity implements HudUpdatedListner {

	public Chart chart;
	public LinearLayout readoutMenu;
	public String[] labels = { "Pitch", "Yaw", "Roll" };
	public List<ChartCheckBox> checkBoxList = new ArrayList<ChartCheckBox>();

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
			ChartCheckBox checkBox = new ChartCheckBox(
					readoutMenu.getContext(), label, chart);
			checkBoxList.add(checkBox);
			readoutMenu.addView(checkBox);
		}
	}

	@Override
	public void onDroneUpdate() {
		updateCheckBox("Pitch", drone.orientation.getPitch());
		chart.update();
	}

	private void updateCheckBox(String label, double value) {
		for (ChartCheckBox box : checkBoxList) {
			if (box.getText().equals(label)) {
				((ChartSeries) box.getTag()).newData(value);
			}
		}
	}

}