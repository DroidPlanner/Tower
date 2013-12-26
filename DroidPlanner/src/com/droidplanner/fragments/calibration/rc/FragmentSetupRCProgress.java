package com.droidplanner.fragments.calibration.rc;

import com.droidplanner.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FragmentSetupRCProgress extends FragmentSetupRCPanel implements
		OnClickListener {
	private TextView textTitle;
	private TextView textProgress;
	private String txt;
	private Button btnCancel;
	private ProgressBar pb;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_rc_progress,
				container, false);
		
		textTitle = (TextView) view.findViewById(R.id.textViewProgressTitle);
		textTitle.setText(txt);
		textProgress = (TextView) view.findViewById(R.id.textViewProgress);
		textProgress.setText("0/0");
		
		pb = (ProgressBar) view.findViewById(R.id.progressBarRCSetup);
		pb.setIndeterminate(true);
		
		btnCancel = (Button) view.findViewById(R.id.ButtonCancel);
		btnCancel.setOnClickListener(this);

		return view;
	}

	public void setText(String text) {
		txt = text;
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.equals(btnCancel)) {
			rcSetupFragment.cancel();
		}
	}

	public void updateProgress(int index, int count) {
		if (textTitle!=null) {
			textTitle.setText(txt);
		}
		if(pb!=null){
			pb.setIndeterminate(false);
			pb.setMax(count);
			pb.setProgress(index);
		}
		if(textProgress!=null){
			textProgress.setText(String.valueOf(index)+"/"+String.valueOf(count));
		}
	}

}
