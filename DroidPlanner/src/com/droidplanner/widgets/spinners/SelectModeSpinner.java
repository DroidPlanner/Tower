package com.droidplanner.widgets.spinners;

import android.content.Context;
import android.widget.Spinner;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneTypeListner;
import com.droidplanner.drone.DroneInterfaces.ModeChangedListener;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;

public class SelectModeSpinner extends SpinnerSelfSelect implements
		OnSpinnerItemSelectedListener, DroneTypeListner, ModeChangedListener {

	private ModeAdapter modeAdapter;
	private Drone drone;
	private Context context;

	public SelectModeSpinner(Context context) {
		super(context);
		this.context = context;
		selectable = false;
		setBackgroundResource(R.drawable.black_button);
	}

	public void buildSpinner(Context context, Drone drone) {
		this.drone = drone;
		setOnSpinnerItemSelectedListener(this);
		this.drone.setModeChangedListener(this);
		this.drone.setDroneTypeChangedListner(this);

		onDroneTypeChanged();
	}

	@Override
	public void onDroneTypeChanged() {
		buildAdapter();
	}

	private void buildAdapter() {
		modeAdapter = new ModeAdapter(this.context,
				android.R.layout.simple_spinner_dropdown_item,
				ApmModes.getModeList(this.drone.type.getType()));
		setAdapter(modeAdapter);
	}

	@Override
	public void onModeChanged() {
		try {
			this.forcedSetSelection(modeAdapter.getPosition(drone.state.getMode()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
		ApmModes newMode = modeAdapter.getItem(position);
		drone.state.changeFlightMode(newMode);		
	}

}
