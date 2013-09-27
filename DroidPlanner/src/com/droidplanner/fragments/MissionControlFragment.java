package com.droidplanner.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.droidplanner.R;

public class MissionControlFragment extends Fragment implements OnClickListener {

	public interface OnMissionControlInteraction {
		public void onJoystickSelected();

		public void onPlanningSelected();

		public void onHUDSelected();
		
		public void onArmSelected();

		public void onDisArmSelected();

		public void onConnectSelected();

		public void onDisConnectSelected();

		public void onRTLSelected();

		public void onLandSelected();

		public void onTakeOffSelected();
	}

	private OnMissionControlInteraction listner;
	private ImageButton hudBtn;
	private ImageButton connectBtn;
	private ImageButton missionBtn;
	private ImageButton joystickBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mission_control,
				container, false);
		setupViews(view);
		setupListner();
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listner = (OnMissionControlInteraction) activity;
	}

	private void setupListner() {
		hudBtn.setOnClickListener(this);
		connectBtn.setOnClickListener(this);
		missionBtn.setOnClickListener(this);
		joystickBtn.setOnClickListener(this);
	}

	private void setupViews(View parentView) {
		hudBtn = (ImageButton) parentView.findViewById(R.id.mc_hudBtn);
		connectBtn = (ImageButton) parentView.findViewById(R.id.mc_connectBtn);
		missionBtn = (ImageButton) parentView.findViewById(R.id.mc_planningBtn);
		joystickBtn = (ImageButton) parentView
				.findViewById(R.id.mc_joystickBtn);

		
		hudBtn.setEnabled(true);
		missionBtn.setEnabled(true);
		joystickBtn.setEnabled(true);
		
		

	}

	private void setButtonState(boolean armEnable, boolean missionEnable) {

		hudBtn.setEnabled(armEnable);
		missionBtn.setEnabled(missionEnable);
		joystickBtn.setEnabled(missionEnable);

		hudBtn.setImageResource(armEnable ? R.drawable.armg : R.drawable.armd);
		missionBtn.setImageResource(missionEnable ? R.drawable.missionb
				: R.drawable.missiond);
		joystickBtn.setImageResource(missionEnable ? R.drawable.gamecontrollerg
				: R.drawable.gamecontrollerd);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.mc_hudBtn:
			listner.onHUDSelected();
			break;
		case R.id.mc_planningBtn:
			listner.onPlanningSelected();
			break;
		case R.id.mc_joystickBtn:
			listner.onJoystickSelected();
			break;
		case R.id.mc_connectBtn:
			listner.onConnectSelected();
			break;
		}
	}

}
