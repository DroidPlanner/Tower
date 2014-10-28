package org.droidplanner.android.activities;

import java.util.concurrent.atomic.AtomicBoolean;

import org.droidplanner.R;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.dialogs.DroneshareDialog;
import org.droidplanner.android.fragments.FlightActionsFragment;
import org.droidplanner.android.fragments.FlightMapFragment;
import org.droidplanner.android.fragments.TelemetryFragment;
import org.droidplanner.android.fragments.mode.FlightModePanel;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class FlightActivity extends DrawerNavigationUI implements OnDroneListener {

    private static final String TAG = FlightActivity.class.getSimpleName();
	private static final int GOOGLE_PLAY_SERVICES_REQUEST_CODE = 101;

    private final AtomicBoolean mSlidingPanelCollapsing = new AtomicBoolean(false);

    private final SlidingUpPanelLayout.PanelSlideListener mDisablePanelSliding = new
            SlidingUpPanelLayout.PanelSlideListener() {
        @Override
        public void onPanelSlide(View view, float v) {}

        @Override
        public void onPanelCollapsed(View view) {
            mSlidingPanel.setSlidingEnabled(false);
            mSlidingPanel.setPanelHeight(mFlightActionsView.getHeight());
            mSlidingPanelCollapsing.set(false);

            //Remove the panel slide listener
            mSlidingPanel.setPanelSlideListener(null);
        }

        @Override
        public void onPanelExpanded(View view) {}

        @Override
        public void onPanelAnchored(View view) {}

        @Override
        public void onPanelHidden(View view) {}
    };

	private FragmentManager fragmentManager;
	private TextView warningView;

	private FlightMapFragment mapFragment;
    private FlightActionsFragment flightActions;
    private TelemetryFragment telemetryFragment;
    private FlightModePanel flightModePanel;

    private SlidingUpPanelLayout mSlidingPanel;
    private View mFlightActionsView;

	private View mLocationButtonsContainer;
	private ImageButton mGoToMyLocation;
	private ImageButton mGoToDroneLocation;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight);

		fragmentManager = getSupportFragmentManager();

        mSlidingPanel = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer);

		warningView = (TextView) findViewById(R.id.failsafeTextView);

		final SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawerRight);
        //Only the phone layout has the sliding drawer
        if(slidingDrawer != null) {
            slidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
                @Override
                public void onDrawerClosed() {
                    final int slidingDrawerWidth = slidingDrawer.getContent().getWidth();
                    final boolean isSlidingDrawerOpened = slidingDrawer.isOpened();
                    updateLocationButtonsMargin(isSlidingDrawerOpened, slidingDrawerWidth);
                }
            });

            slidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
                @Override
                public void onDrawerOpened() {
                    final int slidingDrawerWidth = slidingDrawer.getContent().getWidth();
                    final boolean isSlidingDrawerOpened = slidingDrawer.isOpened();
                    updateLocationButtonsMargin(isSlidingDrawerOpened, slidingDrawerWidth);
                }
            });
        }

		setupMapFragment();

		mLocationButtonsContainer = findViewById(R.id.location_button_container);
		mGoToMyLocation = (ImageButton) findViewById(R.id.my_location_button);
		mGoToDroneLocation = (ImageButton) findViewById(R.id.drone_location_button);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0);
            }
        });

		mGoToMyLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mapFragment != null) {
					mapFragment.goToMyLocation();
					updateMapLocationButtons(AutoPanMode.DISABLED);
				}
			}
		});
		mGoToMyLocation.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (mapFragment != null) {
					mapFragment.goToMyLocation();
					updateMapLocationButtons(AutoPanMode.USER);
					return true;
				}
				return false;
			}
		});

		mGoToDroneLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mapFragment != null) {
					mapFragment.goToDroneLocation();
					updateMapLocationButtons(AutoPanMode.DISABLED);
				}
			}
		});
		mGoToDroneLocation.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (mapFragment != null) {
					mapFragment.goToDroneLocation();
					updateMapLocationButtons(AutoPanMode.DRONE);
					return true;
				}
				return false;
			}
		});

		flightActions = (FlightActionsFragment) fragmentManager.findFragmentById(R.id
                .flightActionsFragment);
		if (flightActions == null) {
			flightActions = new FlightActionsFragment();
			fragmentManager.beginTransaction().add(R.id.flightActionsFragment, flightActions).commit();
		}

        mFlightActionsView = findViewById(R.id.flightActionsFragment);
        mFlightActionsView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(!mSlidingPanelCollapsing.get()) {
                    mSlidingPanel.setPanelHeight(mFlightActionsView.getHeight());
                }
            }
        });

        // Add the telemetry fragment
        telemetryFragment = (TelemetryFragment) fragmentManager.findFragmentById(R.id
                .telemetryFragment);
        if (telemetryFragment == null) {
            telemetryFragment = new TelemetryFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.telemetryFragment, telemetryFragment)
                    .commit();
        }

        // Add the mode info panel fragment
        flightModePanel = (FlightModePanel) fragmentManager.findFragmentById(R.id
                .sliding_drawer_content);
        if (flightModePanel == null) {
            flightModePanel = new FlightModePanel();
            fragmentManager.beginTransaction()
                    .add(R.id.sliding_drawer_content, flightModePanel)
                    .commit();
        }

		DroneshareDialog.perhapsShow(this);
	}

    @Override
    public void onApiConnected(DroidPlannerApi api){
        super.onApiConnected(api);
        if(mapFragment != null){
            mapFragment.onApiConnected(api);
        }

        if(flightActions != null){
            flightActions.onApiConnected(api);
        }

        if(telemetryFragment != null)
            telemetryFragment.onApiConnected(api);

        if(flightModePanel != null)
            flightModePanel.onApiConnected(api);

        enableSlidingUpPanel(this.dpApi);
    }

    @Override
    public void onApiDisconnected(){
        super.onApiDisconnected();
        if(mapFragment != null)
            mapFragment.onApiDisconnected();

        if(flightActions != null)
            flightActions.onApiDisconnected();

        if(telemetryFragment != null)
            telemetryFragment.onApiDisconnected();

        if(flightModePanel != null)
            flightModePanel.onApiDisconnected();
    }

    @Override
    protected int getNavigationDrawerEntryId() {
        return R.id.navigation_flight_data;
    }

    @Override
    protected boolean enableMissionMenus(){
        return true;
    }

    private void updateMapLocationButtons(AutoPanMode mode) {
		mGoToMyLocation.setActivated(false);
		mGoToDroneLocation.setActivated(false);

		if (mapFragment != null) {
			mapFragment.setAutoPanMode(mode);
		}

		switch (mode) {
		case DRONE:
			mGoToDroneLocation.setActivated(true);
			break;

		case USER:
			mGoToMyLocation.setActivated(true);
			break;
		default:
			break;
		}
	}

    public void updateMapBearing(float bearing){
        if(mapFragment != null)
            mapFragment.updateMapBearing(bearing);
    }

	/**
	 * Ensures that the device has the correct version of the Google Play
	 * Services.
	 * 
	 * @return true if the Google Play Services binary is valid
	 */
	private boolean isGooglePlayServicesValid(boolean showErrorDialog) {
		// Check for the google play services is available
		final int playStatus = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());
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
	 * Used to setup the flight screen map fragment. Before attempting to
	 * initialize the map fragment, this checks if the Google Play Services
	 * binary is installed and up to date.
	 */
	private void setupMapFragment() {
		if (mapFragment == null && isGooglePlayServicesValid(true)) {
			mapFragment = (FlightMapFragment) fragmentManager.findFragmentById(R.id.mapFragment);
			if (mapFragment == null) {
				mapFragment = new FlightMapFragment();
				fragmentManager.beginTransaction().add(R.id.mapFragment, mapFragment).commit();
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		setupMapFragment();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		updateMapLocationButtons(mAppPrefs.getAutoPanMode());
	}

	/**
	 * Account for the various ui elements and update the map padding so that it
	 * remains 'visible'.
	 */
	private void updateLocationButtonsMargin(boolean isOpened, int drawerWidth) {

		// Update the right margin for the my location button
		final ViewGroup.MarginLayoutParams marginLp = (ViewGroup.MarginLayoutParams) mLocationButtonsContainer
				.getLayoutParams();
		final int rightMargin = isOpened ? marginLp.leftMargin + drawerWidth : marginLp.leftMargin;
		marginLp.setMargins(marginLp.leftMargin, marginLp.topMargin, rightMargin,
				marginLp.bottomMargin);
        mLocationButtonsContainer.requestLayout();
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		super.onDroneEvent(event, drone);
		switch (event) {
		case AUTOPILOT_WARNING:
			onWarningChanged(drone);
			break;

        case ARMING:
        case CONNECTED:
        case DISCONNECTED:
        case STATE:
            enableSlidingUpPanel(dpApi);
            break;

		default:
			break;
		}
	}

    private void enableSlidingUpPanel(DroidPlannerApi api){
        if (mSlidingPanel == null || api == null) {
            return;
        }

        final boolean isEnabled = flightActions != null && flightActions.isSlidingUpPanelEnabled
                (api);

        if (isEnabled) {
            mSlidingPanel.setSlidingEnabled(true);
        } else {
            if(!mSlidingPanelCollapsing.get()) {
                if (mSlidingPanel.isPanelExpanded()) {
                    mSlidingPanel.setPanelSlideListener(mDisablePanelSliding);
                    mSlidingPanel.collapsePanel();
                    mSlidingPanelCollapsing.set(true);
                } else {
                    mSlidingPanel.setSlidingEnabled(false);
                    mSlidingPanelCollapsing.set(false);
                }
            }
        }
    }

	public void onWarningChanged(Drone drone) {
		if (drone.getState().isWarning()) {
			warningView.setText(drone.getState().getWarning());
			warningView.setVisibility(View.VISIBLE);
		} else {
			warningView.setVisibility(View.GONE);
		}
	}

}
