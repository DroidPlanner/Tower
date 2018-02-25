package co.aerobotics.android.fragments.mode;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ModeRTLFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(co.aerobotics.android.R.layout.fragment_mode_rtl, container, false);
	}
}
