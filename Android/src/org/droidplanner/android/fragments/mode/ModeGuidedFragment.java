package org.droidplanner.android.fragments.mode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.R;
import org.droidplanner.android.api.DroneApi;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

public class ModeGuidedFragment extends ApiListenerFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener {

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
			final DroneApi droneApi = getDroneApi();
			if (droneApi.isConnected())
				droneApi.setGuidedAltitude(newValue);
			break;
		}
	}

	@Override
	public void onApiConnected(DroneApi api) {
		if (mAltitudeWheel != null) {
			final int initialValue = (int) Math.max(api.getGuidedState().getCoordinate()
					.getAltitude(), 2f);
			mAltitudeWheel.setCurrentValue(initialValue);
		}
	}

	@Override
	public void onApiDisconnected() {
	}
}
