package com.droidplanner.activitys;

import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;
import com.droidplanner.widgets.graph.Chart;
import com.droidplanner.widgets.graph.ChartSeries;

public class ChartActivity extends SuperActivity implements
		OnCheckedChangeListener, HudUpdatedListner {

	private Chart chart;
	private LinearLayout readoutMenu;

	String[] labels = { "Pitch", "Yaw", "Roll" };
	
	private List<CheckBox> CheckBoxList;

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
		for (String label : labels) {
				CheckBox checkBox = buildCheckBox(label);
				CheckBoxList.add(checkBox);
				readoutMenu.addView(checkBox);
		}
	}

	private CheckBox buildCheckBox(String label) {
		ChartSeries serie = new ChartSeries(800,Color.RED);
		chart.series.add(serie);
		CheckBox checkBox = new CheckBox(readoutMenu.getContext());
		checkBox.setText(label);
		checkBox.setChecked(serie.isActive());
		checkBox.setGravity(Gravity.LEFT);
		checkBox.setTag(serie);
		checkBox.setOnCheckedChangeListener(this);
		return checkBox;
	}

	@Override
	public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
		 ChartSeries serie = (ChartSeries) checkBox.getTag();
		if (isChecked) {
			serie.enable();
		} else {
			serie.disable();
		}
		chart.update();
	}

	@Override
	public void onDroneUpdate() {
		chart.series.get(0).newData(drone.orientation.getPitch());
		chart.series.get(1).newData(drone.orientation.getRoll());
		chart.series.get(2).newData(drone.orientation.getYaw());
		chart.update();
		
	}

}