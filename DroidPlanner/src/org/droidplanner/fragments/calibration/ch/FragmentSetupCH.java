package org.droidplanner.fragments.calibration.ch;

import org.droidplanner.R;
import org.droidplanner.drone.Drone;
import org.droidplanner.fragments.calibration.SetupMainPanel;
import org.droidplanner.fragments.calibration.SetupSidePanel;

import android.os.Bundle;
import android.view.View;

public class FragmentSetupCH extends SetupMainPanel {

	private int[] valueCH6;
	private int[] valueCH;
	private String[] stringCH6;
	private String[] stringCH;

	private Drone drone;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.drone = parentActivity.drone;
		getCH6Options();
		getCHOptions();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_ch_main;
	}

	@Override
	public SetupSidePanel getSidePanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setupLocalViews(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doCalibrationStep(int step) {
		// TODO Auto-generated method stub

	}

	private void getCHOptions() {
		String pairs[] = getResources().getStringArray(R.array.CH_Options);
		valueCH = null;
		valueCH = new int[pairs.length];
		stringCH = null;
		stringCH = new String[pairs.length];
		
		int i=0;
		for (String item : pairs) {
			String pair[] = item.split(";");
			valueCH[i] = Integer.parseInt(pair[0]);
			stringCH[i] = pair[1];
			i++;
		}
	}

	private void getCH6Options() {
		String pairs[] = getResources().getStringArray(R.array.CH6_Options);
		valueCH6 = null;
		valueCH6 = new int[pairs.length];
		stringCH6 = null;
		stringCH6 = new String[pairs.length];
		
		int i=0;
		for (String item : pairs) {
			String pair[] = item.split(";");
			valueCH6[i] = Integer.parseInt(pair[0]);
			stringCH6[i] = pair[1];
			i++;
		}
	}

}
