package org.droidplanner.fragments.calibration.sf;

import org.droidplanner.R;
import org.droidplanner.calibration.CalParameters;
import org.droidplanner.calibration.SF_CalParameters;
import org.droidplanner.calibration.CalParameters.OnCalibrationEvent;
import org.droidplanner.drone.Drone;
import org.droidplanner.fragments.SetupRadioFragment;
import org.droidplanner.fragments.calibration.FragmentSetupProgress;
import org.droidplanner.fragments.calibration.SetupMainPanel;
import org.droidplanner.fragments.calibration.SetupSidePanel;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

public class FragmentSetupSF extends SetupMainPanel implements OnCalibrationEvent{
	private int[] valueSF;
	private String[] stringSF;
	
	private Drone drone;
	private SF_CalParameters sfParameters;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.drone = parentActivity.drone;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onReadCalibration(CalParameters calParameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSentCalibration(CalParameters calParameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCalibrationData(CalParameters calParameters, int index,
			int count, boolean isSending) {
		if (sidePanel != null && sfParameters != null) {
			String title;
			if (isSending) {
					title = getResources().getString(R.string.setup_ch_desc_uploading);
			} else {
				title = getResources().getString(R.string.setup_ch_desc_downloading);
			}

			((FragmentSetupProgress) sidePanel).updateProgress(index+1, count,
					title);
		}
	}

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_sf_main;
	}

	@Override
	public SetupSidePanel getSidePanel() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setupLocalViews(View v) {
		// TODO Auto-generated method stub
		setupSpinners();
	}

	@Override
	public void doCalibrationStep(int step) {
		// TODO Auto-generated method stub

	}

	private SetupSidePanel getInitialPanel(){
		return sidePanel;		
	}
	
	private SetupSidePanel getProgressPanel() {
		sidePanel = ((SetupRadioFragment) getParentFragment())
				.changeSidePanel(new FragmentSetupProgress());

		return sidePanel;
	}

	private void uploadCalibrationData() {
		
	}
	
	private void downloadCalibrationData() {
		
	}
	
	private void setupSpinners() {
		getSFOptions();

		final ArrayAdapter<String> adapterSF = new ArrayAdapter<String>(parentActivity,R.layout.spinner_setup_item,stringSF);

		adapterSF.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);
	}

	private void getSFOptions() {
		String pairs[] = getResources().getStringArray(R.array.Servo_Functions);
		valueSF = null;
		valueSF = new int[pairs.length];
		stringSF = null;
		stringSF = new String[pairs.length];
		
		int i=0;
		for (String item : pairs) {
			String pair[] = item.split(";");
			valueSF[i] = Integer.parseInt(pair[0]);
			stringSF[i] = pair[1];
			i++;
		}
	}
}
