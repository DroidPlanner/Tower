package com.droidplanner.fragments.calibration.rc;

import com.droidplanner.R;
import com.droidplanner.fragments.RcSetupFragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

public class FragmentSetupRCOptions extends Fragment implements OnClickListener, OnItemSelectedListener{
	public RcSetupFragment rcSetupFragment;
	private Button btnSend;
	private Button btnCancel;
	private Spinner spinnerCH7;
	private Spinner spinnerCH8;
	private int optionCH7;
	private int optionCH8;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_rc_options, container,
				false);
		btnSend = (Button)view.findViewById(R.id.ButtonSend);
		btnSend.setOnClickListener(this);
		
		btnCancel = (Button)view.findViewById(R.id.ButtonCancel);
		btnCancel.setOnClickListener(this);
		
		spinnerCH7 = (Spinner)view.findViewById(R.id.spinnerCH7Features);
		spinnerCH7.setSelection(optionCH7-1,true);
		spinnerCH7.setOnItemSelectedListener(this);
		
		spinnerCH8 = (Spinner)view.findViewById(R.id.spinnerCH8Features);
		spinnerCH8.setSelection(optionCH8-1,true);
		spinnerCH8.setOnItemSelectedListener(this);
		
		return view;
	}

	@Override
	public void onClick(View arg0) {
		if(rcSetupFragment!=null){
			if(arg0.equals(btnSend)){
//				rcSetupFragment.updateRCOptionsData();
			}
			else if(arg0.equals(btnCancel)) {
//				rcSetupFragment.cancel();
			}
		}
	}

	public int getOptionCH7() {
		return optionCH7;
	}

	public void setOptionCH7(int optionCH7) {
		this.optionCH7 = optionCH7;
		if(spinnerCH7!=null){
			spinnerCH7.setSelection(optionCH7-1,true);
		}
	}

	public int getOptionCH8() {
		return optionCH8;
	}

	public void setOptionCH8(int optionCH8) {
		this.optionCH8 = optionCH8;
		if(spinnerCH8!=null){
			spinnerCH8.setSelection(optionCH8-1,true);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if(arg1.equals(spinnerCH7)){
			optionCH7 = arg2;
		}
		else if (arg1.equals(spinnerCH8)){
			optionCH8 = arg2;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}
