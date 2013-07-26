package com.droidplanner.activitys;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;
import com.droidplanner.widgets.graph.Chart;

public class ChartActivity extends SuperActivity implements
		OnCheckedChangeListener, HudUpdatedListner {

	private Chart chart;
	private LinearLayout readoutMenu;

	String[] labels = { "Pitch", "Yaw", "Roll" };

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

		setupOverlay();

		drone.setHudListner(this);
	}

	void setupOverlay() {
		int i = 0;

		for (String label : labels) {
				CheckBox checkBox = new CheckBox(readoutMenu.getContext());
				checkBox.setOnCheckedChangeListener(this);
				checkBox.setText(label);
				checkBox.setChecked(true);
				checkBox.setGravity(Gravity.LEFT);
				checkBox.setTag(i++);
				readoutMenu.addView(checkBox);				
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
		Integer dataIndex = (Integer) checkBox.getTag();
		Log.d("TAG", "tag:"+dataIndex);
		if (isChecked) {
			chart.chartData.enableEntry(dataIndex);
		} else {
			chart.chartData.disableEntry(dataIndex);
		}
		chart.update();
	}

	@Override
	public void onDroneUpdate() {
		chart.chartData.series.get(0).newData(drone.orientation.getPitch());
		chart.chartData.series.get(1).newData(drone.orientation.getRoll());
		chart.chartData.series.get(2).newData(drone.orientation.getYaw());
		chart.update();
		
	}

}