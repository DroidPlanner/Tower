package com.droidplanner.activitys;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;
import com.droidplanner.widgets.graph.Chart;
import com.droidplanner.widgets.graph.ChartCheckBox;
import com.droidplanner.widgets.graph.ChartSeries;

public class ChartActivity extends SuperActivity implements
		OnCheckedChangeListener, HudUpdatedListner {

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

		setupOverlay();

		drone.setHudListner(this);
	}

	void setupOverlay() {
		for (String label : labels) {
			ChartCheckBox checkBox = buildCheckBox(label,
					readoutMenu.getContext(), chart.series);
			CheckBoxList.add(checkBox);
			readoutMenu.addView(checkBox);
		}
	}

	private ChartCheckBox buildCheckBox(String label, Context context, List<ChartSeries> seriesList) {
		ChartSeries serie = new ChartSeries(800, Color.RED);
		seriesList.add(serie);
		ChartCheckBox checkBox = new ChartCheckBox(context);
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