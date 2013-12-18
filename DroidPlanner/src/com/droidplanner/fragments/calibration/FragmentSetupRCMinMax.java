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

public class FragmentSetupRCMinMax extends Fragment implements OnClickListener{
	public RcSetupFragment rcSetupFragment;
	private Button btnNext;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_rc_minmax, container,
				false);
		btnNext = (Button)view.findViewById(R.id.buttonRCCancel);
		btnNext.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View arg0) {
		if(rcSetupFragment!=null){
			rcSetupFragment.changeSetupPanel(2);
		}
	}

}
