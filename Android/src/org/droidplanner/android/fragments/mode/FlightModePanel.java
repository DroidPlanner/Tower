package org.droidplanner.android.fragments.mode;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;

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
public class FlightModePanel extends Fragment implements OnDroneListener {

	/**
	 * This is the parent activity for this fragment.
	 */
	private SuperUI mParentActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof SuperUI)) {
			throw new IllegalStateException(
					"Parent activity must be an instance of "
							+ SuperUI.class.getName());
		}

		mParentActivity = (SuperUI) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mParentActivity = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_flight_mode_panel, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Update the mode info panel based on the current mode.
		onModeUpdate(mParentActivity.drone.state.getMode());
	}

	@Override
	public void onStart() {
		super.onStart();

		if (mParentActivity != null) {
			mParentActivity.drone.events.addDroneListener(this);
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		if (mParentActivity != null) {
			mParentActivity.drone.events.removeDroneListener(this);
		}
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		switch (event) {
		case CONNECTED:
		case DISCONNECTED:
		case MODE:
		case TYPE:
			// Update the mode info panel
			onModeUpdate(drone.state.getMode());
			break;
		default:
			break;
		}
	}

	private void onModeUpdate(ApmModes mode) {
		// Update the info panel fragment
		Fragment infoPanel;
		if (mParentActivity == null
				|| !mParentActivity.drone.MavClient.isConnected()) {
			infoPanel = new ModeDisconnectedFragment();
		} else {
			switch (mode) {
			case ROTOR_RTL:
				infoPanel = new ModeRTLFragment();
				break;
			case ROTOR_AUTO:
				infoPanel = new ModeAutoFragment();
				break;
			case ROTOR_LAND:
				infoPanel = new ModeLandFragment();
				break;
			case ROTOR_LOITER:
				infoPanel = new ModeLoiterFragment();
				break;
			case ROTOR_STABILIZE:
				infoPanel = new ModeStabilizeFragment();
				break;
			case ROTOR_ACRO:
				infoPanel = new ModeAcroFragment();
				break;
			case ROTOR_ALT_HOLD:
				infoPanel = new ModeAltholdFragment();
				break;
			case ROTOR_CIRCLE:
				infoPanel = new ModeCircleFragment();
				break;
			case ROTOR_GUIDED:
				if (((DroidPlannerApp)getActivity().getApplication()).followMe.isEnabled()) {
					infoPanel = new ModeFollowFragment();
				} else {
					infoPanel = new ModeGuidedFragment();
				}
				break;
			case ROTOR_POSITION:
				infoPanel = new ModePositionFragment();
				break;
			case ROTOR_TOY:
				infoPanel = new ModeDriftFragment();
				break;
			case ROTOR_SPORT:
				infoPanel = new ModeSportFragment();
				break;
			case ROTOR_HYBRID:
				infoPanel = new ModeHybridFragment();
				break;
			default:
				infoPanel = new ModeDisconnectedFragment();
				break;
			}
		}

		getChildFragmentManager().beginTransaction()
				.replace(R.id.modeInfoPanel, infoPanel).commit();
	}
}