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
	private Button btnCancel;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_rc_minmax, container,
				false);
		btnNext = (Button)view.findViewById(R.id.ButtonNext);
		btnNext.setOnClickListener(this);
		
		btnCancel = (Button)view.findViewById(R.id.ButtonCancel);
		btnCancel.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View arg0) {
		if(rcSetupFragment!=null){
			if(arg0.equals(btnNext)){
				rcSetupFragment.changeSetupPanel(2);
			}
			else if(arg0.equals(btnCancel)) {
				rcSetupFragment.cancel();
			}
		}
	}

}
