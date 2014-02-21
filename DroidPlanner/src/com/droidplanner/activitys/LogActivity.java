package com.droidplanner.activitys;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.widgets.graph.Chart;
import com.droidplanner.widgets.graph.ChartCheckBoxList;

public class LogActivity extends SuperActivity {


	public Chart chart;
	public LinearLayout readoutMenu;
	public String[] labels = { "Pitch", "Roll","Yaw", "Altitude", "Target Alt.", "Latitude",
			"Longitude","SatCount" ,"Voltage", "Current", "G. Speed", "A. Speed","WP","DistToWp", };
	public ChartCheckBoxList checkBoxList = new ChartCheckBoxList();

	@Override
	public int getNavigationItem() {
		return 7;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logs);

		chart = (Chart) findViewById(R.id.chart);
		readoutMenu = (LinearLayout) findViewById(R.id.readoutMenu);

		checkBoxList.populateView(readoutMenu, labels, chart);

	}


}
