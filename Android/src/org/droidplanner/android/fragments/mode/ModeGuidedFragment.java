package org.droidplanner.android.fragments.mode;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.core.drone.Drone;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ModeGuidedFragment extends Fragment implements OnClickListener {

	private Button altPlus1;
	private Button altPlus10;
	private Button altMinus1;
	private Button altMinus10;
	private TextView altTextView;
	public Drone drone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mode_guided, container,
				false);
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		setupViews(view);
		setupListener();
		updateLabel();
		return view;
	}

	protected void setupViews(View parentView) {
		altPlus1 = (Button) parentView
				.findViewById(R.id.button_altitude_plus_1);
		altPlus10 = (Button) parentView
				.findViewById(R.id.button_altitude_plus_10);
		altMinus1 = (Button) parentView
				.findViewById(R.id.button_altitude_minus_1);
		altMinus10 = (Button) parentView
				.findViewById(R.id.button_altitude_minus_10);
		altTextView = (TextView) parentView.findViewById(R.id.guided_altitude);
	}

	protected void setupListener() {
		altPlus1.setOnClickListener(this);
		altPlus10.setOnClickListener(this);
		altMinus1.setOnClickListener(this);
		altMinus10.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_altitude_plus_1:
			drone.guidedPoint.changeGuidedAltitude(1);
			break;
		case R.id.button_altitude_plus_10:
			drone.guidedPoint.changeGuidedAltitude(10);
			break;
		case R.id.button_altitude_minus_1:
			drone.guidedPoint.changeGuidedAltitude(-1);
			break;
		case R.id.button_altitude_minus_10:
			drone.guidedPoint.changeGuidedAltitude(-10);
			break;
		}
		updateLabel();
	}

	protected void updateLabel() {
		this.altTextView.setText("Target Altitude: ("
				+ drone.guidedPoint.getAltitude()+")");
	}
}
