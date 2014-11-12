package org.droidplanner.android.fragments.mode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ox3dr.services.android.lib.drone.property.GuidedState;

import org.droidplanner.R;
import org.droidplanner.android.api.Drone;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

public class ModeGuidedFragment extends ApiListenerFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener {

    private static final float DEFAULT_ALTITUDE = 2f;

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

		mAltitudeWheel.addChangingListener(this);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mAltitudeWheel != null) {
			mAltitudeWheel.removeChangingListener(this);
		}
	}

	@Override
	public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
		switch (cardWheel.getId()) {
		case R.id.altitude_spinner:
			final Drone drone = getDrone();
			if (drone.isConnected())
				drone.setGuidedAltitude(newValue);
			break;
		}
	}

	@Override
	public void onApiConnected() {
		if (mAltitudeWheel != null) {
            GuidedState guidedState = getDrone().getGuidedState();

			final int initialValue = (int) Math.max(guidedState == null
                    ? DEFAULT_ALTITUDE
                    : guidedState.getCoordinate().getAltitude(),
                    DEFAULT_ALTITUDE);
			mAltitudeWheel.setCurrentValue(initialValue);
		}
	}

	@Override
	public void onApiDisconnected() {
	}
}
