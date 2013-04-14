package com.droidplanner.widgets.spinners;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.MAVLink.Drone;
import com.MAVLink.Messages.ApmModes;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;

public class SelectModeSpinner extends SpinnerSelfSelect implements OnSpinnerItemSelectedListener {
	public interface OnModeSpinnerSelectedListener {
		void OnModeSpinnerSelected(ApmModes apmModes);
	}

	private OnModeSpinnerSelectedListener listener;
	
	private ArrayList<String> modeSpinnerAdapter;

	public SelectModeSpinner(Context context) {
		super(context);
	}
	
	public void buildSpinner(Context context, OnModeSpinnerSelectedListener listener ){
		modeSpinnerAdapter = new ArrayList<String>();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_dropdown_item,
				modeSpinnerAdapter);
		
		updateModeSpinner(null);
		setAdapter(adapter);
		setOnSpinnerItemSelectedListener(this);
		setOnWaypointSpinnerSelectedListener(listener);
	}

	public void updateModeSpinner(Drone drone) {
		modeSpinnerAdapter.clear();
		if (drone != null) {
				updateWpSpinnerWithList(drone);
		}
		updateWpSpinnerWithNoData();
		return;
	}

	private void updateWpSpinnerWithNoData() {
		modeSpinnerAdapter.add("Mode");
	}

	private void updateWpSpinnerWithList(Drone drone) {
		modeSpinnerAdapter.clear();
		for (ApmModes mode : ApmModes.values()) {
			if (mode!=ApmModes.UNKNOWN) {
				modeSpinnerAdapter.add(mode.getName());			
			}
		}
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
		ApmModes mode = ApmModes.getMode(text);
		if (mode != ApmModes.UNKNOWN) {
			listener.OnModeSpinnerSelected(mode);			
		}
	}
	
	
	public void setOnWaypointSpinnerSelectedListener(
			OnModeSpinnerSelectedListener listener) {
		this.listener = listener;
	}
}
