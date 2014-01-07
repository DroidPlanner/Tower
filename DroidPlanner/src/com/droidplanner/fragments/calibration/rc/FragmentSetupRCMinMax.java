package com.droidplanner.fragments.calibration.rc;

import android.support.v4.app.Fragment;
import com.droidplanner.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.droidplanner.fragments.RcSetupFragment;

public class FragmentSetupRCMinMax extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        final RcSetupFragment rcSetupFragment = (RcSetupFragment) getParentFragment();

		final View view = inflater.inflate(R.layout.fragment_setup_rc_minmax, container,	false);
		final Button btnNext = (Button)view.findViewById(R.id.ButtonNext);
		btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rcSetupFragment != null){
                    rcSetupFragment.changeSetupPanel(2);
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
