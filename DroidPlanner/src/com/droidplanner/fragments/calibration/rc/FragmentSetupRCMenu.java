package com.droidplanner.fragments.calibration.rc;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.droidplanner.R;
import com.droidplanner.fragments.RcSetupFragment;

public class FragmentSetupRCMenu extends Fragment implements
		OnClickListener {
	public RcSetupFragment rcSetupFragment;
	private Button btnCalibrate;
	private Button btnRCOption;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_rc_menu, container,
				false);
		btnCalibrate = (Button)view.findViewById(R.id.ButtonRCCalibrate);
		btnCalibrate.setOnClickListener(this);
		
		btnRCOption = (Button)view.findViewById(R.id.ButtonRCOptions);
		btnRCOption.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View arg0) {
		if(rcSetupFragment!=null){
			if(arg0.equals(btnCalibrate)){
				rcSetupFragment.changeSetupPanel(1);
			}
			else if(arg0.equals(btnRCOption)) {
				rcSetupFragment.changeSetupPanel(4);
			}
		}
	}
}
