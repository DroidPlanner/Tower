package org.droidplanner.activities;

import org.droidplanner.R;
import org.droidplanner.activities.helpers.SuperUI;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.drone.variables.missionD.MissionItemD;
import org.droidplanner.drone.variables.missionD.waypoints.SpatialCoordItemD;
import org.droidplanner.fragments.FlightActionsFragment;
import org.droidplanner.fragments.FlightMapFragment;
import org.droidplanner.fragments.RCFragment;
import org.droidplanner.fragments.TelemetryFragment;
import org.droidplanner.fragments.helpers.FlightSlidingDrawerContent;
import org.droidplanner.fragments.helpers.OnMapInteractionListener;
import org.droidplanner.fragments.mode.FlightModePanel;
import org.droidplanner.polygon.PolygonPoint;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SlidingDrawer;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;

public class FlightActivity extends SuperUI implements
		OnMapInteractionListener, FlightActionsFragment.OnMissionControlInteraction, OnDroneListener{

    private static final int GOOGLE_PLAY_SERVICES_REQUEST_CODE = 101;

    private FragmentManager fragmentManager;
	private RCFragment rcFragment;
	private View failsafeTextView;
	private FlightMapFragment mapFragment;
    private Fragment editorTools;

    private SlidingDrawer mSlidingDrawer;

    private boolean mIsPhone;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight);

		fragmentManager = getSupportFragmentManager();
		failsafeTextView = findViewById(R.id.failsafeTextView);

        mSlidingDrawer = (SlidingDrawer) findViewById(R.id.SlidingDrawerRight);
        mSlidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                updateMapPadding();
            }
        });

        mSlidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                updateMapPadding();
            }
        });

        mapFragment = (FlightMapFragment) fragmentManager.findFragmentById(R.id.mapFragment);
        if(mapFragment == null){
            mapFragment = new FlightMapFragment();
            fragmentManager.beginTransaction().add(R.id.mapFragment, mapFragment).commit();
        }

        editorTools = fragmentManager.findFragmentById(R.id.editorToolsFragment);
        if(editorTools == null){
            editorTools = new FlightActionsFragment();
            fragmentManager.beginTransaction().add(R.id.editorToolsFragment, editorTools).commit();
        }

        /*
        Check to see if we're using a phone layout, or a tablet layout.
        If the 'telemetryFragment' view doesn't exist, then we're in phone layout,
        as it was merged with the right sliding drawer because of space constraints.
         */
        View telemetryView = findViewById(R.id.telemetryFragment);
        mIsPhone = telemetryView == null;

        if(mIsPhone){
            Fragment slidingDrawerContent = fragmentManager.findFragmentById(R.id
                    .sliding_drawer_content);
            if (slidingDrawerContent == null) {
                slidingDrawerContent = new FlightSlidingDrawerContent();
                fragmentManager.beginTransaction().add(R.id.sliding_drawer_content,
                        slidingDrawerContent).commit();
            }
        }
        else{
            //Add the telemtry fragment
            Fragment telemetryFragment = fragmentManager.findFragmentById(R.id.telemetryFragment);
            if(telemetryFragment == null){
                telemetryFragment = new TelemetryFragment();
                fragmentManager.beginTransaction().add(R.id.telemetryFragment,
                        telemetryFragment).commit();
            }

            //Add the mode info panel fragment
            Fragment flightModePanel = fragmentManager.findFragmentById(R.id.sliding_drawer_content);
            if(flightModePanel == null){
                flightModePanel = new FlightModePanel();
                fragmentManager.beginTransaction().add(R.id.sliding_drawer_content,
                        flightModePanel).commit();
            }
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
            mapFragment = (FlightMapFragment) fragmentManager.findFragmentById(R.id.mapFragment);
            if (mapFragment == null) {
                mapFragment = new FlightMapFragment();
                fragmentManager.beginTransaction().add(R.id.mapFragment, mapFragment).commit();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        setupMapFragment();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        updateMapPadding();
    }

    /**
     * Account for the various ui elements and update the map padding so that it remains 'visible'.
     */
    private void updateMapPadding(){
        int rightPadding = getSlidingDrawerWidth();

        int bottomPadding = 0;
        int leftPadding = 0;
        if(mIsPhone){
            final View editorToolsView = editorTools.getView();
            ViewGroup.LayoutParams lp = editorToolsView.getLayoutParams();
            if(lp.height == ViewGroup.LayoutParams.MATCH_PARENT){
                leftPadding = editorToolsView.getRight();
            }

            if(lp.width == ViewGroup.LayoutParams.MATCH_PARENT){
                bottomPadding = editorToolsView.getHeight();
            }
        }
        mapFragment.mMap.setPadding(leftPadding, 0, rightPadding, bottomPadding);
    }

    private int getSlidingDrawerWidth(){
        if(mSlidingDrawer.isOpened()){
            return mSlidingDrawer.getContent().getWidth();
        }
        return 0;
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
	public void onMoveWaypoint(SpatialCoordItemD waypoint, LatLng latLng) {
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
	public boolean onMarkerClick(MissionItemD wp) {
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
             mapFragment.saveCameraPosition();
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
	public void onMovingWaypoint(SpatialCoordItemD source, LatLng latLng) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		super.onDroneEvent(event,drone);
		switch (event) {
		case FAILSAFE:
			onFailsafeChanged(drone);
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

	@Override
	public CharSequence[][] getHelpItems() {
		return new CharSequence[][] {
				{ "Spline", "DP v3" },
				{ "https://www.youtube.com/watch?v=v9ydP-NWoJE",
						"https://www.youtube.com/watch?v=miwWUgX6nwY" } };
	}

}
