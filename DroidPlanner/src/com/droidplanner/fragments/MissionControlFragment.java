package com.droidplanner.fragments;

import com.droidplanner.DroidPlannerApp.ConnectionStateListner;
import com.droidplanner.DroidPlannerApp.OnSystemArmListener;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.MAVLink.MavLinkArm;
import com.droidplanner.activitys.PlanningActivity;
import com.droidplanner.drone.Drone;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class MissionControlFragment extends Fragment implements
		OnClickListener, ConnectionStateListner, OnSystemArmListener {

	private ImageButton armBtn;
	private ImageButton rtlBtn;
	private ImageButton landBtn;
	private ImageButton launchBtn;
	private ImageButton connectBtn;
	private ImageButton missionBtn;
	private ImageButton joystickBtn;

	private View view;
	private Drone drone;
	private ConnectionStateListner connectionStateListener;
	private OnSystemArmListener systemArmListener;

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

	private void setButtonState(boolean armEnable, boolean missionEnable) {

		armBtn.setEnabled(armEnable);
		rtlBtn.setEnabled(missionEnable);
		landBtn.setEnabled(missionEnable);
		launchBtn.setEnabled(missionEnable);
		missionBtn.setEnabled(missionEnable);
		joystickBtn.setEnabled(missionEnable);

		armBtn.setImageResource(armEnable ? R.drawable.armg : R.drawable.armd);
		rtlBtn.setImageResource(missionEnable ? R.drawable.rtlb
				: R.drawable.rtld);
		landBtn.setImageResource(missionEnable ? R.drawable.landb
				: R.drawable.landd);
		launchBtn.setImageResource(missionEnable ? R.drawable.launchb
				: R.drawable.launchd);
		missionBtn.setImageResource(missionEnable ? R.drawable.missionb
				: R.drawable.missiond);
		joystickBtn.setImageResource(missionEnable ? R.drawable.gamecontrollerg
				: R.drawable.gamecontrollerd);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.mc_armBtn:
			if (drone.MavClient.isConnected()) {
				if (!drone.state.isArmed()) {
					armBtn.setImageResource(R.drawable.arma);
					drone.tts.speak("Arming the vehicle, please standby");
				}
				MavLinkArm.sendArmMessage(drone, !drone.state.isArmed());
			}
			break;
		case R.id.mc_rtlBtn:
			break;
		case R.id.mc_landBtn:
			break;
		case R.id.mc_launchBtn:
			break;
		case R.id.mc_connectBtn:
			drone.MavClient.toggleConnectionState();
			break;
		case R.id.mc_missionBtn:
			break;
		case R.id.mc_joystickBtn:
			break;

		}
	}

	@Override
	public void notifyConnected() {
		connectBtn.setImageResource(R.drawable.connectg);
		setButtonState(true, false);
		connectionStateListener.notifyConnected();
	}

	@Override
	public void notifyDisconnected() {
		connectBtn.setImageResource(R.drawable.disconnectr);
		setButtonState(false, false);
		connectionStateListener.notifyDisconnected();
	}

	@Override
	public void notifyArmed() {
		setButtonState(true, true);
		systemArmListener.notifyArmed();
	}

	@Override
	public void notifyDisarmed() {
		setButtonState(true, false);

		systemArmListener.notifyDisarmed();

	}

	public void setLister(PlanningActivity planningActivity) {
		// TODO Auto-generated method stub
		this.drone = planningActivity.drone;
		connectionStateListener = planningActivity.app.conectionListner;
		systemArmListener = planningActivity.app.onSystemArmListener;

		planningActivity.app.conectionListner = this;
		planningActivity.app.onSystemArmListener = this;

	}
}
