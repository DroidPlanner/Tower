package com.droidplanner.fragments;

import com.droidplanner.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class MissionControlFragment extends Fragment implements OnClickListener {

	private ImageButton armBtn;
	private ImageButton rtlBtn;
	private ImageButton landBtn;
	private ImageButton launchBtn;
	private ImageButton connectBtn;
	private ImageButton missionBtn;
	private ImageButton joystickBtn;

	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_mission_control, container,
				false);
		setupViews();
		setupListner();
		return view;
	}

	private void setupListner() {
		armBtn.setOnClickListener(this);
		rtlBtn.setOnClickListener(this);
		landBtn.setOnClickListener(this);
		launchBtn.setOnClickListener(this);
		connectBtn.setOnClickListener(this);
		missionBtn.setOnClickListener(this);
		joystickBtn.setOnClickListener(this);
	}

	private void setupViews() {
		armBtn = (ImageButton) view.findViewById(R.id.mc_armBtn);
		rtlBtn = (ImageButton) view.findViewById(R.id.mc_rtlBtn);
		landBtn = (ImageButton) view.findViewById(R.id.mc_landBtn);
		launchBtn = (ImageButton) view.findViewById(R.id.mc_launchBtn);
		connectBtn = (ImageButton) view.findViewById(R.id.mc_connectBtn);
		missionBtn = (ImageButton) view.findViewById(R.id.mc_missionBtn);
		joystickBtn = (ImageButton) view.findViewById(R.id.mc_joystickBtn);
		
		armBtn.setEnabled(false);
		rtlBtn.setEnabled(false);
		landBtn.setEnabled(false);
		launchBtn.setEnabled(false);
		missionBtn.setEnabled(false);
		joystickBtn.setEnabled(false);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.mc_armBtn:
			break;
		case R.id.mc_rtlBtn:
			break;
		case R.id.mc_landBtn:
			break;
		case R.id.mc_launchBtn:
			break;
		case R.id.mc_connectBtn:
				armBtn.setEnabled(!armBtn.isEnabled());
				if(armBtn.isEnabled())
					armBtn.setImageResource(R.drawable.armg);
				else
					armBtn.setImageResource(R.drawable.armd);
					
			break;
		case R.id.mc_missionBtn:
			break;
		case R.id.mc_joystickBtn:
			break;

		}
	}
}
