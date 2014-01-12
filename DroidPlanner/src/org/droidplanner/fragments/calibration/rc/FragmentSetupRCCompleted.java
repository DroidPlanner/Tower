package org.droidplanner.fragments.calibration.rc;

import org.droidplanner.fragments.RcSetupFragment;

import android.support.v4.app.Fragment;
import org.droidplanner.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentSetupRCCompleted extends Fragment {

    public final static String EXTRA_TEXT_SUMMARY = FragmentSetupRCCompleted.class.getName() + "" +
            ".extra.TEXT_SUMMARY";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        final RcSetupFragment rcSetupFragment = (RcSetupFragment) getParentFragment();

		final View view = inflater.inflate(R.layout.fragment_setup_rc_completed, container,	false);
		final TextView textView = (TextView)view.findViewById(R.id.textViewSummary);

        final Bundle args = getArguments();
        if(args != null){
            textView.setText(args.getString(EXTRA_TEXT_SUMMARY, ""));
        }

		final Button btnSend = (Button)view.findViewById(R.id.ButtonSend);
		btnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rcSetupFragment != null){
                    rcSetupFragment.updateCalibrationData();
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
		return view;
	}

}
