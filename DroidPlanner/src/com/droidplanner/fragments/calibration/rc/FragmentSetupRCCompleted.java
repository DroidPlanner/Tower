package com.droidplanner.fragments.calibration.rc;

import com.droidplanner.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentSetupRCCompleted extends FragmentSetupRCPanel implements OnClickListener{
	private TextView textView;
	private String txt;
	private Button btnSend;
	private Button btnCancel;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_rc_completed, container,
				false);
		textView = (TextView)view.findViewById(R.id.textViewSummary);
		textView.setText(txt);

		btnSend = (Button)view.findViewById(R.id.ButtonSend);
		btnSend.setOnClickListener(this);
		
		btnCancel = (Button)view.findViewById(R.id.ButtonCancel);
		btnCancel.setOnClickListener(this);
		return view;
	}

	public void setText(String text){
		txt = text;
	}
	
	@Override
	public void onClick(View arg0) {
		if(rcSetupFragment!=null){
			if(arg0.equals(btnSend)){
//				rcSetupFragment.updateCalibrationData();
			}
			else if(arg0.equals(btnCancel)) {
//				rcSetupFragment.cancel();
			}
		}
	}

}
