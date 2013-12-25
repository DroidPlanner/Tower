package com.droidplanner.fragments.calibration.rc;

import com.droidplanner.R;
import com.droidplanner.fragments.RcSetupFragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FragmentSetupRCOptions extends Fragment implements OnClickListener{
	public RcSetupFragment rcSetupFragment;
	private Button btnSend;
	private Button btnCancel;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_rc_options, container,
				false);
		btnSend = (Button)view.findViewById(R.id.ButtonSend);
		btnSend.setOnClickListener(this);
		
		btnCancel = (Button)view.findViewById(R.id.ButtonCancel);
		btnCancel.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View arg0) {
		if(rcSetupFragment!=null){
			if(arg0.equals(btnSend)){
				rcSetupFragment.updateRCOptionsData();
			}
			else if(arg0.equals(btnCancel)) {
				rcSetupFragment.cancel();
			}
		}
	}

}
