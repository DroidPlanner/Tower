package org.droidplanner.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ox3dr.services.android.lib.drone.event.Event;
import com.ox3dr.services.android.lib.drone.property.Type;

import org.droidplanner.R;
import org.droidplanner.android.api.DroneApi;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;

public class FlightActionsFragment extends ApiListenerFragment {

	interface SlidingUpHeader {
		boolean isSlidingUpPanelEnabled(DroneApi api);
	}

	private static final IntentFilter eventFilter = new IntentFilter(Event.EVENT_TYPE_UPDATED);

	private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (Event.EVENT_TYPE_UPDATED.equals(action)) {
				selectActionsBar(getDroneApi().getType().getDroneType());
			}
		}
	};

	private SlidingUpHeader header;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_flight_actions_bar, container, false);
	}

	@Override
	public void onApiConnected() {
        DroneApi drone = getDroneApi();
        if(drone.isConnected())
		    selectActionsBar(drone.getType().getDroneType());
        else{
            selectActionsBar(-1);
        }
		getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
	}

	@Override
	public void onApiDisconnected() {
		getBroadcastManager().unregisterReceiver(eventReceiver);
	}

	private void selectActionsBar(int droneType) {
		final FragmentManager fm = getChildFragmentManager();

		Fragment actionsBarFragment;
		switch (droneType) {
		case Type.TYPE_COPTER:
			actionsBarFragment = new CopterFlightActionsFragment();
			break;

		case Type.TYPE_PLANE:
			actionsBarFragment = new PlaneFlightActionsFragment();
			break;

		case Type.TYPE_ROVER:
		default:
			actionsBarFragment = new GenericActionsFragment();
			break;
		}

		fm.beginTransaction().replace(R.id.flight_actions_bar, actionsBarFragment).commit();
		header = (SlidingUpHeader) actionsBarFragment;
	}

	public boolean isSlidingUpPanelEnabled(DroneApi api) {
		return header != null && header.isSlidingUpPanelEnabled(api);
	}
}
