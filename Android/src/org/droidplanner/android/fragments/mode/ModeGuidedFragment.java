package org.droidplanner.android.fragments.mode;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.model.Drone;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ModeGuidedFragment extends Fragment implements CardWheelHorizontalView.OnCardWheelChangedListener {

	public Drone drone;

    private CardWheelHorizontalView mAltitudeWheel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
		return inflater.inflate(R.layout.fragment_mode_guided, container, false);
	}

    @Override
	public void onViewCreated(View parentView, Bundle savedInstanceState) {
        super.onViewCreated(parentView, savedInstanceState);

        final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(getActivity()
                .getApplicationContext(), R.layout.wheel_text_centered, 2, 200, "%d m");

        mAltitudeWheel = (CardWheelHorizontalView) parentView.findViewById(R.id.altitude_spinner);
        mAltitudeWheel.setViewAdapter(altitudeAdapter);

        final int initialValue = (int) Math.max(drone.getGuidedPoint().getAltitude()
                        .valueInMeters(), GuidedPoint.getMinAltitude(drone));
        mAltitudeWheel.setCurrentValue(initialValue);
        mAltitudeWheel.addChangingListener(this);
	}

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if(mAltitudeWheel != null) {
            mAltitudeWheel.removeChangingListener(this);
        }
    }

    @Override
    public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
        switch(cardWheel.getId()){
            case R.id.altitude_spinner:
                drone.getGuidedPoint().changeGuidedAltitude(newValue);
                break;
        }
    }
}
