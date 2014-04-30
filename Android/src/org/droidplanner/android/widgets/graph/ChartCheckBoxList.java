package org.droidplanner.android.widgets.graph;

import java.util.ArrayList;
import java.util.List;

import android.widget.LinearLayout;

public class ChartCheckBoxList {
	private List<ChartCheckBox> checkBoxList = new ArrayList<ChartCheckBox>();

	public void populateView(LinearLayout view, String[] labels, Chart chart) {
		for (String label : labels) {
			ChartCheckBox checkBox = new ChartCheckBox(view.getContext(),
					label, chart);
			checkBoxList.add(checkBox);
			view.addView(checkBox);
		}
	}

	public void updateCheckBox(String label, double value) {
		for (ChartCheckBox box : checkBoxList) {
			if (box.getText().equals(label)) {
				((ChartSeries) box.getTag()).newData(value);
			}
		}
	}

}