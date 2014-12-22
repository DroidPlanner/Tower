package org.droidplanner.android.fragments.mode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.GuidedState;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

public class ModeGuidedFragment extends ApiListenerFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener {

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

		mAltitudeWheel.addScrollListener(this);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (mAltitudeWheel != null) {
			mAltitudeWheel.removeChangingListener(this);
		}
	}

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, int startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {

    }

    @Override
	public void onScrollingEnded(CardWheelHorizontalView cardWheel, int startValue, int endValue) {
		switch (cardWheel.getId()) {
		case R.id.altitude_spinner:
			final Drone drone = getDrone();
			if (drone.isConnected())
				drone.setGuidedAltitude(endValue);
			break;
		}
	}

	@Override
	public void onApiConnected() {
		if (mAltitudeWheel != null) {
            GuidedState guidedState = getDrone().getAttribute(AttributeType.GUIDED_STATE);

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
