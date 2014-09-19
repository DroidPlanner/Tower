package org.droidplanner.android.fragments.mode;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.gcs.follow.FollowAlgorithm.FollowModes;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.model.Drone;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class ThreePVTuningFragment extends Fragment implements View.OnClickListener,
		OnItemSelectedListener, OnDroneListener {
	private Button altPlus1;
	private Button altPlus10;
	private Button altMinus1;
	private Button altMinus10;
	private TextView altTextView;
	private Button radiusPlus1;
	private Button radiusMinus1;
	private TextView radiusTextView;
	private Follow followMe;
	private Spinner spinner;
	private ArrayAdapter<FollowModes> adapter;
	private Drone drone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		DroidPlannerApp app = (DroidPlannerApp) getActivity().getApplication();
		followMe = app.followMe;
		drone = app.getDrone();
		View view = inflater.inflate(R.layout.three_pv_tuning_fragment, container, false);
		setupViews(view);
		setupListener();
		updateLabel();

		drone.addDroneListener(this);
		return view;
	}

	protected void setupViews(View parentView) {
		altPlus1 = (Button) parentView.findViewById(R.id.button_altitude_plus_1);
		altPlus10 = (Button) parentView.findViewById(R.id.button_altitude_plus_10);
		altMinus1 = (Button) parentView.findViewById(R.id.button_altitude_minus_1);
		altMinus10 = (Button) parentView.findViewById(R.id.button_altitude_minus_10);
		altTextView = (TextView) parentView.findViewById(R.id.guided_altitude);
		radiusPlus1 = (Button) parentView.findViewById(R.id.button_radius_plus_1);
		radiusMinus1 = (Button) parentView.findViewById(R.id.button_radius_minus_1);
		radiusTextView = (TextView) parentView.findViewById(R.id.follow_radius);
		spinner = (Spinner) parentView.findViewById(R.id.follow_type_spinner);
		adapter = new ArrayAdapter<FollowModes>(getActivity(),
				android.R.layout.simple_spinner_item, FollowModes.values());
		spinner.setAdapter(adapter);
	}

	protected void setupListener() {
		altPlus1.setOnClickListener(this);
		altPlus10.setOnClickListener(this);
		altMinus1.setOnClickListener(this);
		altMinus10.setOnClickListener(this);
		radiusPlus1.setOnClickListener(this);
		radiusMinus1.setOnClickListener(this);
		spinner.setOnItemSelectedListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_radius_minus_1:
				followMe.changeRadius(-1.0);
				break;
			case R.id.button_radius_plus_1:
				followMe.changeRadius(+1.0);
				break;
			case R.id.button_altitude_plus_1:
				drone.getGuidedPoint().changeGuidedAltitude(1);
				break;
			case R.id.button_altitude_plus_10:
				drone.getGuidedPoint().changeGuidedAltitude(10);
				break;
			case R.id.button_altitude_minus_1:
				drone.getGuidedPoint().changeGuidedAltitude(-1);
				break;
			case R.id.button_altitude_minus_10:
				drone.getGuidedPoint().changeGuidedAltitude(-10);
				break;
			default:
				break;
		}
		updateLabel();
	}

	protected void updateLabel() {
		this.altTextView.setText("Target Altitude: (" + drone.getGuidedPoint().getAltitude() + ")");
		Length radius = followMe.getRadius();
		if (radiusTextView != null) {
			this.radiusTextView.setText("Radius: (" + radius + ")");
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		followMe.setType(adapter.getItem(position));
		updateLabel();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
			case FOLLOW_CHANGE_TYPE:
				spinner.setSelection(adapter.getPosition(followMe.getType()));
				break;
			default:
				break;
		}

	}
}
