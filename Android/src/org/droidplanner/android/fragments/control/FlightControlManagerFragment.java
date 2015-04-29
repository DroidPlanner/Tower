package org.droidplanner.android.fragments.control;

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

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Type;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;

public class FlightControlManagerFragment extends ApiListenerFragment {

	public interface SlidingUpHeader {
		boolean isSlidingUpPanelEnabled(Drone drone);
	}

	private static final IntentFilter eventFilter = new IntentFilter();
    static {
        eventFilter.addAction(AttributeEvent.TYPE_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
    }

	private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.STATE_CONNECTED:
                case AttributeEvent.TYPE_UPDATED:
                    Type type = getDrone().getAttribute(AttributeType.TYPE);
                    selectActionsBar(type == null ? -1 : type.getDroneType());
                    break;
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
        Drone drone = getDrone();
        if(drone.isConnected()) {
            Type type = getDrone().getAttribute(AttributeType.TYPE);
            selectActionsBar(type.getDroneType());
        }
        else{
            selectActionsBar(-1);
        }
		getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
	}

	@Override
	public void onApiDisconnected() {
		getBroadcastManager().unregisterReceiver(eventReceiver);
        if(isResumed())
            selectActionsBar(-1);
	}

	private void selectActionsBar(int droneType) {
		final FragmentManager fm = getChildFragmentManager();

		Fragment actionsBarFragment;
		switch (droneType) {
		case Type.TYPE_COPTER:
			actionsBarFragment = new CopterFlightControlFragment();
			break;

		case Type.TYPE_PLANE:
			actionsBarFragment = new PlaneFlightControlFragment();
			break;

		case Type.TYPE_ROVER:
            actionsBarFragment = new RoverFlightControlFragment();
            break;

		default:
			actionsBarFragment = new GenericActionsFragment();
			break;
		}

		fm.beginTransaction().replace(R.id.flight_actions_bar, actionsBarFragment).commitAllowingStateLoss();
		header = (SlidingUpHeader) actionsBarFragment;
	}

	public boolean isSlidingUpPanelEnabled(Drone api) {
		return header != null && header.isSlidingUpPanelEnabled(api);
	}
}
