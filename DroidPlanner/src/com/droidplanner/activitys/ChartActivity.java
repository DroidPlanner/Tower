package com.droidplanner.activitys;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;
import com.droidplanner.widgets.graph.Chart;

public class ChartActivity extends SuperActivity implements
		OnCheckedChangeListener, HudUpdatedListner {

	private Chart chart;
	private TableLayout layout;

	// Settings overlay layout
	private int nRows = 3;
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
		layout = (TableLayout) findViewById(R.id.readoutMenu);

		setupOverlay();

		drone.setHudListner(this);
	}

	void setupOverlay() {
		int i = 0;

		for (int y = 0; y < nRows; y++) {
			TableRow tr = new TableRow(this);
			layout.addView(tr);
			if (i < labels.length) {
				CheckBox cb = new CheckBox(layout.getContext());
				cb.setOnCheckedChangeListener(this);

				tr.addView(cb);
				cb.setTag(i);
				cb.setText(labels[i]);
				cb.setTextColor(chart.chartData.series.get((Integer) cb.getTag()).getColor());
				cb.setChecked(chart.chartData.isActive((Integer) cb.getTag()));
				i++;
			}
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton bv, boolean isChecked) {
		Integer dataIndex = (Integer) bv.getTag();
		if (isChecked) {
			chart.chartData.enableEntry(dataIndex);
		} else {
			chart.chartData.disableEntry(dataIndex);
		}
	}

	@Override
	public void onDroneUpdate() {
		chart.chartData.series.get(0).newData(drone.orientation.getPitch());
		chart.chartData.series.get(1).newData(drone.orientation.getRoll());
		chart.chartData.series.get(2).newData(drone.orientation.getYaw());
		chart.update();
		
	}

}