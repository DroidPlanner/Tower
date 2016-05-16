package org.droidplanner.android.fragments.mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowState;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;

/**
 * Implements the flight/apm mode panel description.
 */
public class FlightModePanel extends ApiListenerFragment{

    private final static IntentFilter eventFilter = new IntentFilter();
    static {
        eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_VEHICLE_MODE);
        eventFilter.addAction(AttributeEvent.TYPE_UPDATED);
        eventFilter.addAction(AttributeEvent.FOLLOW_START);
        eventFilter.addAction(AttributeEvent.FOLLOW_STOP);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(getActivity() == null)
                return;

            onModeUpdate(getDrone());
        }
    };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_flight_mode_panel, container, false);
	}

	@Override
	public void onApiConnected() {
		// Update the mode info panel based on the current mode.
		onModeUpdate(getDrone());
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
	}

	@Override
	public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(eventReceiver);
	}

	private void onModeUpdate(Drone drone) {
		// Update the info panel fragment
        final State droneState = drone.getAttribute(AttributeType.STATE);
		Fragment infoPanel;
		if (droneState == null || !droneState.isConnected()) {
			infoPanel = new ModeDisconnectedFragment();
		} else {
            VehicleMode mode = droneState.getVehicleMode();
            if(mode == null){
                infoPanel = new ModeDefaultFragment();
            }
            else {
                switch (mode) {
                    case COPTER_RTL:
                    case PLANE_RTL:
                    case ROVER_RTL:
                        infoPanel = new ModeRTLFragment();
                        break;

                    case COPTER_AUTO:
                    case PLANE_AUTO:
                    case ROVER_AUTO:
                        infoPanel = new ModeAutoFragment();
                        break;

                    case COPTER_LAND:
                        infoPanel = new ModeLandFragment();
                        break;

                    case COPTER_LOITER:
                    case PLANE_LOITER:
                        infoPanel = new ModeLoiterFragment();
                        break;

                    case COPTER_STABILIZE:
                    case PLANE_STABILIZE:
                        infoPanel = new ModeStabilizeFragment();
                        break;

                    case COPTER_ACRO:
                        infoPanel = new ModeAcroFragment();
                        break;

                    case COPTER_ALT_HOLD:
                        infoPanel = new ModeAltholdFragment();
                        break;

                    case COPTER_CIRCLE:
                    case PLANE_CIRCLE:
                        infoPanel = new ModeCircleFragment();
                        break;

                    case COPTER_GUIDED:
                    case PLANE_GUIDED:
                    case ROVER_GUIDED:
                    case ROVER_HOLD:
                        final FollowState followState = drone.getAttribute(AttributeType.FOLLOW_STATE);
                        if (followState.isEnabled()) {
                            infoPanel = new ModeFollowFragment();
                        } else {
                            infoPanel = new ModeGuidedFragment();
                        }
                        break;

                    case COPTER_DRIFT:
                        infoPanel = new ModeDriftFragment();
                        break;

                    case COPTER_SPORT:
                        infoPanel = new ModeSportFragment();
                        break;

                    case COPTER_POSHOLD:
                        infoPanel = new ModePosHoldFragment();
                        break;

                    default:
                        infoPanel = new ModeDefaultFragment();
                        break;
                }
            }
		}

		getChildFragmentManager().beginTransaction().replace(R.id.modeInfoPanel, infoPanel).commitAllowingStateLoss();
	}
}