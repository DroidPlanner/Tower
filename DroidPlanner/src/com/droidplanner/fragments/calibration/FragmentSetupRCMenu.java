package com.droidplanner.fragments.calibration;

import com.droidplanner.R;
import com.droidplanner.fragments.RcSetupFragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentSetupRCMenu extends Fragment implements OnClickListener{
	public RcSetupFragment rcSetupFragment;
	private Button btnCalibration;
	private Button btnFailsafe;
	private Button btnRCOptions;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_rc_menu, container,
				false);
		btnCalibration = (Button)view.findViewById(R.id.Button01);
		btnFailsafe = (Button)view.findViewById(R.id.Button02);
		btnRCOptions = (Button)view.findViewById(R.id.Button03);
		
		btnCalibration.setOnClickListener(this);
		btnFailsafe.setOnClickListener(this);
		btnRCOptions.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onClick(View arg0) {
		if(rcSetupFragment!=null){
			if(arg0.equals(btnCalibration)){
				rcSetupFragment.changeSetupPanel(1);
			}
			if(arg0.equals(btnFailsafe)){
				rcSetupFragment.changeSetupPanel(4);				
			}
			if(arg0.equals(btnRCOptions)){
				rcSetupFragment.changeSetupPanel(5);				
			}
		}
	}

}
