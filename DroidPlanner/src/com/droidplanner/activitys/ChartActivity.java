package com.droidplanner.activitys;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.widgets.graph.Chart;

public class ChartActivity extends SuperActivity implements
		OnCheckedChangeListener {

	private Chart chart;
	private TableLayout layout;

	// Settings overlay layout
	private int nColumns = 3;
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
		
		layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d("CHART", "newData");
				double[] data = {0,1,2};
				chart.newFlightData(data );				
			}
		});

		chart.setDataSize(labels.length);
		chart.setNames(labels);
		
		chart.enableEntry(0);
		chart.enableEntry(1);
		chart.enableEntry(2);
		
		setupOverlay();
	}

	void setupOverlay() {
		int i = 0;

		TableRow tr = new TableRow(this);
		layout.addView(tr);
		for (int x = 0; x < nColumns; x++) {
			if (i < labels.length) {
				CheckBox cb = new CheckBox(layout.getContext());
				cb.setOnCheckedChangeListener(this);

				tr.addView(cb);
				cb.setTag(i);
				cb.setText(labels[i]);
				cb.setTextColor(Color.WHITE);
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

}