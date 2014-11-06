package org.droidplanner.android.fragments.mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ox3dr.services.android.lib.drone.event.Event;
import com.ox3dr.services.android.lib.gcs.follow.FollowState;
import com.ox3dr.services.android.lib.gcs.follow.FollowType;

import org.droidplanner.R;
import org.droidplanner.android.api.DroneApi;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

public class ModeFollowFragment extends ModeGuidedFragment implements OnItemSelectedListener {

	private static final IntentFilter eventFilter = new IntentFilter(Event.EVENT_FOLLOW_UPDATE);

	private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (Event.EVENT_FOLLOW_UPDATE.equals(action)) {
				final FollowState followState = getDroneApi().getFollowState();
				if (followState != null) {
					spinner.setSelection(adapter.getPosition(followState.getMode()));
				}
			}
		}
	};

	private Spinner spinner;
	private ArrayAdapter<FollowType> adapter;

	private CardWheelHorizontalView mRadiusWheel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_mode_follow, container, false);
	}

	@Override
	public void onViewCreated(View parentView, Bundle savedInstanceState) {
		super.onViewCreated(parentView, savedInstanceState);

		final Context context = getActivity().getApplicationContext();

		final NumericWheelAdapter radiusAdapter = new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 0, 200, "%d m");

		mRadiusWheel = (CardWheelHorizontalView) parentView.findViewById(R.id.radius_spinner);
		mRadiusWheel.setViewAdapter(radiusAdapter);
		updateCurrentRadius();
		mRadiusWheel.addChangingListener(this);

		spinner = (Spinner) parentView.findViewById(R.id.follow_type_spinner);
		adapter = new ArrayAdapter<FollowType>(getActivity(), android.R.layout.simple_spinner_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (mRadiusWheel != null) {
			mRadiusWheel.removeChangingListener(this);
		}
	}

	@Override
	public void onApiConnected(DroneApi api) {
		super.onApiConnected(api);

		adapter.addAll(api.getFollowTypes());
		getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
	}

	@Override
	public void onApiDisconnected() {
		super.onApiDisconnected();
		getBroadcastManager().unregisterReceiver(eventReceiver);
	}

	@Override
	public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
		switch (cardWheel.getId()) {
		case R.id.radius_spinner:
			final DroneApi droneApi = getDroneApi();
			if (droneApi.isConnected())
				droneApi.setFollowMeRadius(newValue);
			break;

		default:
			super.onChanged(cardWheel, oldValue, newValue);
			break;
		}
	}

	private void updateCurrentRadius() {
		final DroneApi droneApi = getDroneApi();
		if (mRadiusWheel != null && droneApi.isConnected()) {
			mRadiusWheel.setCurrentValue((int) droneApi.getFollowState().getRadius());
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		final DroneApi droneApi = getDroneApi();
		if (droneApi.isConnected()) {
			droneApi.enableFollowMe(adapter.getItem(position));
			updateCurrentRadius();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
}
