package com.droidplanner.fragments.calibration.rc;

import android.support.v4.app.Fragment;
import com.droidplanner.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.droidplanner.fragments.RcSetupFragment;

/**
 * This fragment displays the progress for the RC setup process.
 */
public class FragmentSetupRCProgress extends Fragment {
	private TextView textTitle;
	private TextView textProgress;
	private ProgressBar pb;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        final RcSetupFragment rcSetupFragment = (RcSetupFragment) getParentFragment();

		final View view = inflater.inflate(R.layout.fragment_setup_rc_progress,
				container, false);
		
		textTitle = (TextView) view.findViewById(R.id.textViewProgressTitle);

		textProgress = (TextView) view.findViewById(R.id.textViewProgress);
		textProgress.setText("0/0");
		
		pb = (ProgressBar) view.findViewById(R.id.progressBarRCSetup);
		pb.setIndeterminate(true);
		
		final Button btnCancel = (Button) view.findViewById(R.id.ButtonCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rcSetupFragment != null) {
                    rcSetupFragment.cancel();
                }
            }
        });

		return view;
	}

	public void updateProgress(int index, int count, String txt) {
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
