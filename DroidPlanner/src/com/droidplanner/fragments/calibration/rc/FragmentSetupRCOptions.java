package com.droidplanner.fragments.calibration.rc;

import android.support.v4.app.Fragment;
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
import com.droidplanner.fragments.RcSetupFragment;

public class FragmentSetupRCOptions extends Fragment {
	private Spinner spinnerCH7;
	private Spinner spinnerCH8;
	private int optionCH7;
	private int optionCH8;
	private int[] optionVal = {0,2,3,4,5,7,8,9,10,11,12,13,14,16,17,18,19};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        final RcSetupFragment rcSetupFragment = (RcSetupFragment) getParentFragment();

		final View view = inflater.inflate(R.layout.fragment_setup_rc_options, container,	false);
		final Button btnSend = (Button)view.findViewById(R.id.ButtonSend);
		btnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rcSetupFragment != null){
                    rcSetupFragment.updateRCOptionsData();
                }
            }
        });
		
		final Button btnCancel = (Button)view.findViewById(R.id.ButtonCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rcSetupFragment != null){
                    rcSetupFragment.cancel();
                }
            }
        });
		
		spinnerCH7 = (Spinner)view.findViewById(R.id.spinnerCH7Features);
		spinnerCH7.setSelection(getSpinnerPosition(optionCH7),true);
		
		spinnerCH8 = (Spinner)view.findViewById(R.id.spinnerCH8Features);
		spinnerCH8.setSelection(getSpinnerPosition(optionCH8),true);
		
		return view;
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
}
