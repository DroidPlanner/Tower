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

		chart.setDataSize(labels.length);

		chart.enableEntry(0);
		chart.enableEntry(1);
		chart.enableEntry(2);

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
				cb.setTextColor(chart.getEntryColor((Integer) cb.getTag()));
				cb.setChecked(chart.isActive((Integer) cb.getTag()));
				i++;
			}
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton bv, boolean isChecked) {
		Integer dataIndex = (Integer) bv.getTag();

		if (isChecked) {
			// Add dataIndex to rendered items
			int c = chart.enableEntry(dataIndex);
			if (c == -1) {
				bv.setChecked(false);

			}
		} else {
			chart.disableEntry(dataIndex);

		}

	}

	@Override
	public void onDroneUpdate() {
		double[] data = { drone.orientation.getPitch(),
				drone.orientation.getRoll(), drone.orientation.getYaw() };
		chart.newFlightData(data);
	}

}