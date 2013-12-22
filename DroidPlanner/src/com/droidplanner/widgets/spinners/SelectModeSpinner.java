package com.droidplanner.widgets.spinners;

import android.content.Context;
import android.widget.Spinner;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;

public class SelectModeSpinner extends SpinnerSelfSelect implements
		OnSpinnerItemSelectedListener, OnDroneListner {

	private ModeAdapter modeAdapter;
	private Drone drone;
	private Context context;

	public SelectModeSpinner(Context context) {
		super(context);
		this.context = context;
		selectable = false;
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case TYPE:
			buildAdapter(drone);			
			break;
		case MODE:
			onModeChanged(drone);
		default:
			break;
		}		
	}

	public void onModeChanged(Drone drone) {
		try {
			this.forcedSetSelection(modeAdapter.getPosition(drone.state.getMode()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void buildSpinner(Context context, Drone drone) {
		this.drone = drone;
		setOnSpinnerItemSelectedListener(this);
		buildAdapter(drone);
	}

	private void buildAdapter(Drone drone) {
		modeAdapter = new ModeAdapter(this.context,
				android.R.layout.simple_spinner_dropdown_item,
				ApmModes.getModeList(this.drone.type.getType()));
		setAdapter(modeAdapter);
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
		ApmModes newMode = modeAdapter.getItem(position);
		drone.state.changeFlightMode(newMode);
	}

}
