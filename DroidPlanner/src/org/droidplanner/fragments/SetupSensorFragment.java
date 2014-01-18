package org.droidplanner.fragments;

import org.droidplanner.fragments.helpers.SuperSetupFragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * This fragment is used to calibrate the drone's compass, and accelerometer.
 */
public class SetupSensorFragment extends SuperSetupFragment {

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

    @Override
    public void onDetach(){
        super.onDetach();    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void setupSpinnerItems(ArrayAdapter<String> adapter) {
		adapter.add("ACC Calibration");
		adapter.add("Compass Calibration");
	}

}
