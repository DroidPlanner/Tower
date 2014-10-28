package org.droidplanner.android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.R;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.variables.Type;
import org.droidplanner.core.model.Drone;

public class FlightActionsFragment extends ApiListenerFragment implements OnDroneListener {

	interface SlidingUpHeader {
		boolean isSlidingUpPanelEnabled(DroidPlannerApi api);
	}

	private SlidingUpHeader header;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_flight_actions_bar, container, false);
	}

	@Override
	public void onApiConnected(DroidPlannerApi api) {
		selectActionsBar(api.getDrone().getType());
		api.addDroneListener(this);
	}

	@Override
	public void onApiDisconnected() {
		getApi().removeDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case TYPE:
			final int droneType = drone.getType();
			selectActionsBar(droneType);
			break;
		}
	}

	private void selectActionsBar(int droneType) {
		final FragmentManager fm = getChildFragmentManager();

		Fragment actionsBarFragment;
		if (Type.isCopter(droneType)) {
			actionsBarFragment = new CopterFlightActionsFragment();
		} else if (Type.isPlane(droneType)) {
			actionsBarFragment = new PlaneFlightActionsFragment();
		} else {
			actionsBarFragment = new GenericActionsFragment();
		}

		fm.beginTransaction().replace(R.id.flight_actions_bar, actionsBarFragment).commit();
		header = (SlidingUpHeader) actionsBarFragment;
	}

	public boolean isSlidingUpPanelEnabled(DroidPlannerApi api) {
		return header != null && header.isSlidingUpPanelEnabled(api);
	}
}
