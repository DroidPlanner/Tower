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
import android.widget.TextView;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.LengthWheelAdapter;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

public class ModeFollowFragment extends ModeGuidedFragment implements OnItemSelectedListener {

	private static final IntentFilter eventFilter = new IntentFilter(AttributeEvent.FOLLOW_UPDATE);

	private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (AttributeEvent.FOLLOW_UPDATE.equals(action)) {
				final FollowState followState = getDrone().getAttribute(AttributeType.FOLLOW_STATE);
				if (followState != null) {
					spinner.setSelection(adapter.getPosition(followState.getMode()));
				}
			}
		}
	};

	private Spinner spinner;
	private ArrayAdapter<FollowType> adapter;

	private CardWheelHorizontalView<LengthUnit> mRadiusWheel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_mode_follow, container, false);
	}

	@Override
	public void onViewCreated(View parentView, Bundle savedInstanceState) {
		super.onViewCreated(parentView, savedInstanceState);

		final Context context = getContext();
        final LengthUnitProvider lengthUP = getLengthUnitProvider();
		final LengthWheelAdapter radiusAdapter = new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(2), lengthUP.boxBaseValueToTarget(200));

		mRadiusWheel = (CardWheelHorizontalView<LengthUnit>) parentView.findViewById(R.id.radius_spinner);
		mRadiusWheel.setViewAdapter(radiusAdapter);
		mRadiusWheel.addScrollListener(this);

		spinner = (Spinner) parentView.findViewById(R.id.follow_type_spinner);
		adapter = new FollowTypesAdapter(context);
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
	public void onApiConnected() {
		super.onApiConnected();
        updateCurrentRadius();
		getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
	}

	@Override
	public void onApiDisconnected() {
		super.onApiDisconnected();
		getBroadcastManager().unregisterReceiver(eventReceiver);
	}

	@Override
	public void onScrollingEnded(CardWheelHorizontalView cardWheel, LengthUnit oldValue, LengthUnit newValue) {
		switch (cardWheel.getId()) {
		case R.id.radius_spinner:
			final Drone drone = getDrone();
			if (drone.isConnected())
				drone.setFollowMeRadius(newValue.toBase().getValue());
			break;

		default:
			super.onScrollingEnded(cardWheel, oldValue, newValue);
			break;
		}
	}

	private void updateCurrentRadius() {
		final Drone drone = getDrone();
		if (mRadiusWheel != null && drone.isConnected()) {
            final FollowState followState = getDrone().getAttribute(AttributeType.FOLLOW_STATE);
			mRadiusWheel.setCurrentValue((getLengthUnitProvider().boxBaseValueToTarget(followState.getRadius())));
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		final Drone drone = getDrone();
		if (drone.isConnected()) {
			drone.enableFollowMe(adapter.getItem(position));
			updateCurrentRadius();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

    private static class FollowTypesAdapter extends ArrayAdapter<FollowType> {

        private final LayoutInflater inflater;

        public FollowTypesAdapter(Context context) {
            super(context, 0, FollowType.values());
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            TextView view;
            if(convertView == null){
                view = (TextView) inflater.inflate(R.layout.list_item_follow_types, parent, false);
            }
            else{
                view = (TextView) convertView;
            }

            final FollowType followType = getItem(position);
            view.setText(followType.getTypeLabel());
            return view;
       }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent){
            return getView(position, convertView, parent);
        }
    }
}
