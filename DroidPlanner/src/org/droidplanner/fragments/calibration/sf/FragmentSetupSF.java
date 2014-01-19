package org.droidplanner.fragments.calibration.sf;

import org.droidplanner.R;
import org.droidplanner.fragments.calibration.SetupMainPanel;
import org.droidplanner.fragments.calibration.SetupSidePanel;

import android.view.View;

public class FragmentSetupSF extends SetupMainPanel {
	private int[] valueSF;
	private String[] stringSF;

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

	}

	@Override
	public void doCalibrationStep(int step) {
		// TODO Auto-generated method stub

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
