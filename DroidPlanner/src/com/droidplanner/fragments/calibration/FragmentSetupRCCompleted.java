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
import android.widget.TextView;

public class FragmentSetupRCCompleted extends Fragment implements OnClickListener{
	public RcSetupFragment rcSetupFragment;
	private Button btnNext;
	private TextView textView;
	private String txt;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_rc_completed, container,
				false);
		textView = (TextView)view.findViewById(R.id.textViewSummary);
		textView.setText(txt);
		btnNext = (Button)view.findViewById(R.id.buttonRCCancel);
		btnNext.setOnClickListener(this);
		return view;
	}
	public void setText(String text){
		txt = text;
	}
	
	@Override
	public void onClick(View arg0) {
		if(rcSetupFragment!=null){
			rcSetupFragment.updateCalibrationData();
		}
	}

}
