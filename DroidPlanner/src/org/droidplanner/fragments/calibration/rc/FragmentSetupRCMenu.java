package org.droidplanner.fragments.calibration.rc;

import org.droidplanner.fragments.RcSetupFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import org.droidplanner.R;

public class FragmentSetupRCMenu extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        final RcSetupFragment rcSetupFragment = (RcSetupFragment) getParentFragment();

		final View view = inflater.inflate(R.layout.fragment_setup_rc_menu, container, false);

		final Button btnCalibrate = (Button)view.findViewById(R.id.ButtonRCCalibrate);
		btnCalibrate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rcSetupFragment != null){
                    rcSetupFragment.changeSetupPanel(1);
                }
            }
        });
		
		final Button btnRCOption = (Button)view.findViewById(R.id.ButtonRCOptions);
		btnRCOption.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rcSetupFragment != null){
                    rcSetupFragment.changeSetupPanel(4);
                }
            }
        });

		return view;
	}
}
