package org.droidplanner.android.fragments.mode;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.gcs.follow.FollowAlgorithm.FollowModes;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.model.Drone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class ModeFollowFragment extends ModeGuidedFragment implements OnClickListener,
		OnItemSelectedListener, OnDroneListener {
	private Button radiusPlus1;
	private Button radiusMinus1;
	private TextView radiusTextView;
	private Follow followMe;
	private Spinner spinner;
	private ArrayAdapter<FollowModes> adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		DroidPlannerApp app = (DroidPlannerApp) getActivity().getApplication();
		followMe = app.followMe;
		drone = app.getDrone();
		View view = inflater.inflate(R.layout.fragment_mode_follow, container, false);
		setupViews(view);
		setupListener();
		updateLabel();

		drone.addDroneListener(this);
		return view;
	}

	@Override
	protected void setupViews(View parentView) {
		radiusPlus1 = (Button) parentView.findViewById(R.id.button_radius_plus_1);
		radiusMinus1 = (Button) parentView.findViewById(R.id.button_radius_minus_1);
		radiusTextView = (TextView) parentView.findViewById(R.id.follow_radius);
		spinner = (Spinner) parentView.findViewById(R.id.follow_type_spinner);
		adapter = new ArrayAdapter<FollowModes>(getActivity(),
				android.R.layout.simple_spinner_item, FollowModes.values());
		spinner.setAdapter(adapter);
		super.setupViews(parentView);
	}

	@Override
	protected void setupListener() {
		radiusPlus1.setOnClickListener(this);
		radiusMinus1.setOnClickListener(this);
		spinner.setOnItemSelectedListener(this);
		super.setupListener();
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
		default:
			super.onClick(v);
			break;
		}
		updateLabel();
	}

	@Override
	protected void updateLabel() {
		super.updateLabel();
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
