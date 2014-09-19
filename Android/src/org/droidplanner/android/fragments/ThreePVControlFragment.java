package org.droidplanner.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.droidplanner.R;

public class ThreePVControlFragment extends Fragment implements View.OnClickListener{
	private Button takeoffLandButton;
	private Button followLoiterButton;
	private TextView status;

	protected void setupViews(View parentView) {
		takeoffLandButton = (Button)parentView.findViewById(R.id.button_takeoff_land);
		followLoiterButton = (Button)parentView.findViewById(R.id.button_follow_loiter);
		status = (EditText) parentView.findViewById(R.id.three_pv_status);
	}

	protected void setupListener() {
		takeoffLandButton.setOnClickListener(this);
		followLoiterButton.setOnClickListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.three_pv_control_fragment, container, false);
		setupViews(view);
		setupListener();
		return view;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.button_takeoff_land:
				break;
			case R.id.button_follow_loiter:
				break;
		}
	}
}
