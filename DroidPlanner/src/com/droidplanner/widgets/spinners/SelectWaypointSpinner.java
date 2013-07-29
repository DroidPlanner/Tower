package com.droidplanner.widgets.spinners;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.droidplanner.drone.Drone;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;

public class SelectWaypointSpinner extends SpinnerSelfSelect implements
		OnSpinnerItemSelectedListener {
	public interface OnWaypointSpinnerSelectedListener {
		void OnWaypointSpinnerSelected(int item);
	}

	private OnWaypointSpinnerSelectedListener listener;

	private ArrayList<String> wpSpinnerAdapter;

	public SelectWaypointSpinner(Context context) {
		super(context);
	}

	public void buildSpinner(Context context,
			OnWaypointSpinnerSelectedListener listener) {
		wpSpinnerAdapter = new ArrayList<String>();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_dropdown_item, wpSpinnerAdapter);

		updateWpSpinner(null);
		setAdapter(adapter);
		setOnSpinnerItemSelectedListener(this);
		setOnWaypointSpinnerSelectedListener(listener);
	}

	public void updateWpSpinner(Drone drone) {
		wpSpinnerAdapter.clear();
		if (drone != null) {
			if (drone.mission.getWaypoints().size() > 0) {
				updateWpSpinnerWithList(drone);
				return;
			}
		}
		updateWpSpinnerWithNoData();
		return;
	}

	private void updateWpSpinnerWithNoData() {
		wpSpinnerAdapter.add("WP");
	}

	private void updateWpSpinnerWithList(Drone drone) {
		for (int i = 0; i < drone.mission.getWaypoints().size(); i++) {
			wpSpinnerAdapter.add("WP " + Integer.toString(i + 1));
		}
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
		listener.OnWaypointSpinnerSelected(position + 1);
	}

	public void setOnWaypointSpinnerSelectedListener(
			OnWaypointSpinnerSelectedListener listener) {
		this.listener = listener;
	}
}
