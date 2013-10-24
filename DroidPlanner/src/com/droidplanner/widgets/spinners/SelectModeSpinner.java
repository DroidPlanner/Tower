package com.droidplanner.widgets.spinners;

import android.content.Context;
import android.widget.Spinner;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneTypeListner;
import com.droidplanner.drone.DroneInterfaces.ModeChangedListener;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;

public class SelectModeSpinner extends SpinnerSelfSelect implements
		OnSpinnerItemSelectedListener, DroneTypeListner, ModeChangedListener {
	public interface OnModeSpinnerSelectedListener {
		void OnModeSpinnerSelected(ApmModes apmModes);
	}

	private OnModeSpinnerSelectedListener listener;

	private ModeAdapter modeAdapter;

	private Drone drone;

	private Context context;

	public SelectModeSpinner(Context context) {
		super(context);
		this.context = context;
	}

	public void buildSpinner(Context context,
			OnModeSpinnerSelectedListener listener, Drone drone) {

		this.drone = drone;
		this.listener = listener;
		setOnSpinnerItemSelectedListener(this);
		//this.drone.setModeChangedListener(this);
		//this.drone.setDroneTypeChangedListner(this);
		
		buildAdapter();
	}

	private void buildAdapter() {
		modeAdapter = new ModeAdapter(this.context,
				android.R.layout.simple_spinner_dropdown_item,
				ApmModes.getModeList(this.drone.type.getType()));
		setAdapter(modeAdapter);
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
		listener.OnModeSpinnerSelected(modeAdapter.getItem(position));
	}

	@Override
	public void onModeChanged() {
		
	}

	@Override
	public void onDroneTypeChanged() {
		buildAdapter();
	}

}
