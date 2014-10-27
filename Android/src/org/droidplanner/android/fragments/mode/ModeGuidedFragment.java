package org.droidplanner.android.fragments.mode;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.fragments.helpers.ApiSubscriberFragment;
import org.droidplanner.android.helpers.ApiInterface;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.model.Drone;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ModeGuidedFragment extends ApiSubscriberFragment implements CardWheelHorizontalView
        .OnCardWheelChangedListener {

	protected Drone drone;

    private CardWheelHorizontalView mAltitudeWheel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_mode_guided, container, false);
	}

    @Override
	public void onViewCreated(View parentView, Bundle savedInstanceState) {
        super.onViewCreated(parentView, savedInstanceState);

        final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(getActivity()
                .getApplicationContext(), R.layout.wheel_text_centered, 2, 200, "%d m");

        mAltitudeWheel = (CardWheelHorizontalView) parentView.findViewById(R.id.altitude_spinner);
        mAltitudeWheel.setViewAdapter(altitudeAdapter);

        if(drone != null){
            final int initialValue = (int) Math.max(drone.getGuidedPoint().getAltitude()
                    .valueInMeters(), GuidedPoint.getMinAltitude(drone));
            mAltitudeWheel.setCurrentValue(initialValue);
        }

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
                if(drone != null)
                    drone.getGuidedPoint().changeGuidedAltitude(newValue);
                break;
        }
    }

    @Override
    protected void onApiConnectedImpl(DroidPlannerApi api) {
        drone = api.getDrone();
        if(mAltitudeWheel != null) {
            final int initialValue = (int) Math.max(drone.getGuidedPoint().getAltitude()
                    .valueInMeters(), GuidedPoint.getMinAltitude(drone));
            mAltitudeWheel.setCurrentValue(initialValue);
        }
    }

    @Override
    protected void onApiDisconnectedImpl() {

    }
}
