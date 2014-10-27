package org.droidplanner.android.fragments.mode;

import org.droidplanner.R;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.fragments.helpers.ApiSubscriberFragment;
import org.droidplanner.android.helpers.ApiInterface;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.MAVLink.Messages.ApmModes;

/**
 * Implements the flight/apm mode panel description.
 */
public class FlightModePanel extends ApiSubscriberFragment implements OnDroneListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_flight_mode_panel, container, false);
	}

    @Override
    protected void onApiConnectedImpl(DroidPlannerApi api) {
        api.addDroneListener(this);

        // Update the mode info panel based on the current mode.
        onModeUpdate(api.getState().getMode());
    }

    @Override
    protected void onApiDisconnectedImpl() {
        getApi().removeDroneListener(this);
    }

    @Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		switch (event) {
		case CONNECTED:
		case DISCONNECTED:
		case MODE:
		case TYPE:
		case FOLLOW_START:
		case FOLLOW_STOP:
			// Update the mode info panel
			onModeUpdate(drone.getState().getMode());
			break;
		default:
			break;
		}
	}

	private void onModeUpdate(ApmModes mode) {
		// Update the info panel fragment
        final DroidPlannerApi dpApi = getApi();
		Fragment infoPanel;
		if (dpApi == null || !dpApi.isConnected()) {
			infoPanel = new ModeDisconnectedFragment();
		} else {
			switch (mode) {
			case ROTOR_RTL:
			case FIXED_WING_RTL:
			case ROVER_RTL:
				infoPanel = new ModeRTLFragment();
				break;

			case ROTOR_AUTO:
			case FIXED_WING_AUTO:
			case ROVER_AUTO:
				infoPanel = new ModeAutoFragment();
				break;

			case ROTOR_LAND:
				infoPanel = new ModeLandFragment();
				break;

			case ROTOR_LOITER:
			case FIXED_WING_LOITER:
				infoPanel = new ModeLoiterFragment();
				break;

			case ROTOR_STABILIZE:
			case FIXED_WING_STABILIZE:
				infoPanel = new ModeStabilizeFragment();
				break;

			case ROTOR_ACRO:
				infoPanel = new ModeAcroFragment();
				break;

			case ROTOR_ALT_HOLD:
				infoPanel = new ModeAltholdFragment();
				break;

			case ROTOR_CIRCLE:
			case FIXED_WING_CIRCLE:
				infoPanel = new ModeCircleFragment();
				break;

			case ROTOR_GUIDED:
			case FIXED_WING_GUIDED:
			case ROVER_GUIDED:
				if (dpApi.getFollowMe().isEnabled()) {
					infoPanel = new ModeFollowFragment();
				} else {
					infoPanel = new ModeGuidedFragment();
				}
				break;

			case ROTOR_TOY:
				infoPanel = new ModeDriftFragment();
				break;

			case ROTOR_SPORT:
				infoPanel = new ModeSportFragment();
				break;

			case ROTOR_POSHOLD:
				infoPanel = new ModePosHoldFragment();
				break;

			default:
				infoPanel = new ModeDisconnectedFragment();
				break;
			}
		}

		getChildFragmentManager().beginTransaction().replace(R.id.modeInfoPanel, infoPanel)
				.commit();
	}
}