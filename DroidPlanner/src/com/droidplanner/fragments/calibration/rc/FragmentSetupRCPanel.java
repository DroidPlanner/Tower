package com.droidplanner.fragments.calibration.rc;

import com.droidplanner.fragments.RcSetupFragment;

import android.app.Fragment;
import android.view.View.OnClickListener;

public abstract class FragmentSetupRCPanel extends Fragment implements OnClickListener{
	public RcSetupFragment rcSetupFragment;
	public abstract void setupListener();

}
