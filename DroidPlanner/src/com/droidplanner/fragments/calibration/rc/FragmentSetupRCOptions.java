package com.droidplanner.fragments.calibration.rc;

import com.droidplanner.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

public class FragmentSetupRCOptions extends FragmentSetupRCPanel implements OnClickListener, OnItemSelectedListener{
	private Button btnSend;
	private Button btnCancel;
	private Spinner spinnerCH7;
	private Spinner spinnerCH8;
	private int optionCH7;
	private int optionCH8;
	private int[] optionVal = {0,2,3,4,5,7,8,9,10,11,12,13,14,16,17,18,19};
	
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
		spinnerCH7.setSelection(getSpinnerPosition(optionCH7),true);
		
		spinnerCH8 = (Spinner)view.findViewById(R.id.spinnerCH8Features);
		spinnerCH8.setSelection(getSpinnerPosition(optionCH8),true);
		
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

	public int getOptionCH7() {
		return getSpinnerValue(spinnerCH7.getSelectedItemPosition());
	}

	public int getOptionCH8() {
		return getSpinnerValue(spinnerCH8.getSelectedItemPosition());
	}

	public void setOptionCH7(int optionCH7) {
		this.optionCH7 = optionCH7;
		if(spinnerCH7!=null){
			spinnerCH7.setSelection(getSpinnerPosition(optionCH7), true);
		}
	}

	public void setOptionCH8(int optionCH8) {
		this.optionCH8 = optionCH8;
		if(spinnerCH8!=null){
			spinnerCH8.setSelection(getSpinnerPosition(optionCH8), true);
		}
	}

	private int getSpinnerPosition(int value){
		for(int i=0;i<17;i++){
			if(optionVal[i]==value)
				return i;
		}		
		return -1;
	}
	private int getSpinnerValue(int pos){
		try {
			return optionVal[pos];
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}
