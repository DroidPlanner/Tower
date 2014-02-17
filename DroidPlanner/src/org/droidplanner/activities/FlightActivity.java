package org.droidplanner.activities;

import org.droidplanner.R;
import org.droidplanner.activities.helpers.SuperUI;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.drone.variables.mission.waypoints.SpatialCoordItem;
import org.droidplanner.fragments.FlightActionsFragment;
import org.droidplanner.fragments.FlightActionsFragment.OnMissionControlInteraction;
import org.droidplanner.fragments.FlightMapFragment;
import org.droidplanner.fragments.RCFragment;
import org.droidplanner.fragments.TelemetryFragment;
import org.droidplanner.fragments.helpers.DroneMap;
import org.droidplanner.fragments.helpers.OnMapInteractionListener;
import org.droidplanner.fragments.mode.ModeAcroFragment;
import org.droidplanner.fragments.mode.ModeAltholdFragment;
import org.droidplanner.fragments.mode.ModeAutoFragment;
import org.droidplanner.fragments.mode.ModeCircleFragment;
import org.droidplanner.fragments.mode.ModeDisconnectedFragment;
import org.droidplanner.fragments.mode.ModeDriftFragment;
import org.droidplanner.fragments.mode.ModeGuidedFragment;
import org.droidplanner.fragments.mode.ModeLandFragment;
import org.droidplanner.fragments.mode.ModeLoiterFragment;
import org.droidplanner.fragments.mode.ModePositionFragment;
import org.droidplanner.fragments.mode.ModeRTLFragment;
import org.droidplanner.fragments.mode.ModeStabilizeFragment;
import org.droidplanner.polygon.PolygonPoint;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;

public class FlightActivity extends SuperUI implements
		OnMapInteractionListener, OnMissionControlInteraction, OnDroneListener{

    private static final int GOOGLE_PLAY_SERVICES_REQUEST_CODE = 101;

    private FragmentManager fragmentManager;
	private RCFragment rcFragment;
	private View failsafeTextView;
	private Fragment modeInfoPanel;
	private Fragment mapFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight);

		fragmentManager = getSupportFragmentManager();
		modeInfoPanel = fragmentManager.findFragmentById(R.id.modeInfoPanel);
		failsafeTextView = findViewById(R.id.failsafeTextView);

        //Load the activity fragments
        Fragment modeRtl = fragmentManager.findFragmentById(R.id.modeInfoPanel);
        if(modeRtl == null){
            modeRtl = new ModeRTLFragment();
            fragmentManager.beginTransaction().add(R.id.modeInfoPanel, modeRtl).commit();
        }

        Fragment telemetryFragment = fragmentManager.findFragmentById(R.id.telemetryFragment);
        if(telemetryFragment == null){
            telemetryFragment = new TelemetryFragment();
            fragmentManager.beginTransaction().add(R.id.telemetryFragment,
                    telemetryFragment).commit();
        }

        Fragment editorTools = fragmentManager.findFragmentById(R.id.editorToolsFragment);
        if(editorTools == null){
            editorTools = new FlightActionsFragment();
            fragmentManager.beginTransaction().add(R.id.editorToolsFragment, editorTools).commit();
        }

    }

    /**
     * Ensures that the device has the correct version of the Google Play Services.
     * @return true if the Google Play Services binary is valid
     */
    private boolean isGooglePlayServicesValid(boolean showErrorDialog){
        //Check for the google play services is available
        final int playStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable
                (getApplicationContext());
        final boolean isValid = playStatus == ConnectionResult.SUCCESS;

        if (!isValid && showErrorDialog) {
            final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(playStatus, this,
                    GOOGLE_PLAY_SERVICES_REQUEST_CODE, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });

            if (errorDialog != null)
                errorDialog.show();
        }

        return isValid;
    }

    /**
     * Used to setup the flight screen map fragment. Before attempting to initialize the map
     * fragment, this checks if the Google Play Services binary is installed and up to date.
     */
    private void setupMapFragment(){
        if(mapFragment == null && isGooglePlayServicesValid(true)) {
            mapFragment = fragmentManager.findFragmentById(R.id.mapFragment);
            if (mapFragment == null) {
                mapFragment = new FlightMapFragment();
                fragmentManager.beginTransaction().add(R.id.mapFragment, mapFragment).commit();
            }
        }
    }

	@Override
	protected void onStart() {
		super.onStart();
		onModeChanged(drone);	// Update the mode detail panel;
	}

    @Override
    public void onResume(){
        super.onResume();
        setupMapFragment();
    }

	@Override
	public void onAddPoint(LatLng point) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMoveHome(LatLng coord) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMoveWaypoint(SpatialCoordItem waypoint, LatLng latLng) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMovePolygonPoint(PolygonPoint source, LatLng newCoord) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onMarkerClick(MissionItem wp) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onJoystickSelected() {
		toggleRCFragment();
	}

	@Override
	public void onPlanningSelected() {
        if (mapFragment != null) {
            ((DroneMap) mapFragment ).saveCameraPosition();
        }

        Intent navigationIntent;
		navigationIntent = new Intent(this, EditorActivity.class);
		startActivity(navigationIntent);
	}

	private void toggleRCFragment() {
		if (rcFragment == null) {
			rcFragment = new RCFragment();
			fragmentManager.beginTransaction()
					.add(R.id.containerRC, rcFragment).commit();
		} else {
			fragmentManager.beginTransaction().remove(rcFragment).commit();
			rcFragment = null;
		}
	}

	@Override
	public void onMovingWaypoint(SpatialCoordItem source, LatLng latLng) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		super.onDroneEvent(event,drone);
		switch (event) {
		case FAILSAFE:
			onFailsafeChanged(drone);
			break;
		case MODE:
			onModeChanged(drone);
			break;
		default:
			break;
		}

	}

	public void onFailsafeChanged(Drone drone) {
		if (drone.state.isFailsafe()) {
			failsafeTextView.setVisibility(View.VISIBLE);
		} else {
			failsafeTextView.setVisibility(View.GONE);
		}
	}

	public void onModeChanged(Drone drone) {
		switch (drone.state.getMode()) {
		case ROTOR_RTL:
			modeInfoPanel = new ModeRTLFragment();
			break;
		case ROTOR_AUTO:
			modeInfoPanel = new ModeAutoFragment();
			break;
		case ROTOR_LAND:
			modeInfoPanel = new ModeLandFragment();
			break;
		case ROTOR_LOITER:
			modeInfoPanel = new ModeLoiterFragment();
			break;
		case ROTOR_STABILIZE:
			modeInfoPanel = new ModeStabilizeFragment();
			break;
		case ROTOR_ACRO:
			modeInfoPanel = new ModeAcroFragment();
			break;
		case ROTOR_ALT_HOLD:
			modeInfoPanel = new ModeAltholdFragment();
			break;
		case ROTOR_CIRCLE:
			modeInfoPanel = new ModeCircleFragment();
			break;
		case ROTOR_GUIDED:
			modeInfoPanel = new ModeGuidedFragment();
			break;
		case ROTOR_POSITION:
			modeInfoPanel = new ModePositionFragment();
			break;
		case ROTOR_TOY:
			modeInfoPanel = new ModeDriftFragment();
			break;
		default:
			modeInfoPanel = new ModeDisconnectedFragment();
			break;
		}
		if (!drone.MavClient.isConnected()) {
			modeInfoPanel = new ModeDisconnectedFragment();
		}
		fragmentManager.beginTransaction()
				.replace(R.id.modeInfoPanel, modeInfoPanel).commit();
	}

	@Override
	public CharSequence[][] getHelpItems() {
		return new CharSequence[][] {
				{ "Spline", "DP v3" },
				{ "https://www.youtube.com/watch?v=v9ydP-NWoJE",
						"https://www.youtube.com/watch?v=miwWUgX6nwY" } };
	}

}
