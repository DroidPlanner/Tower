package org.droidplanner.android.activities;

import org.droidplanner.R;
import org.droidplanner.android.dialogs.DroneshareDialog;
import org.droidplanner.android.fragments.FlightActionsFragment;
import org.droidplanner.android.fragments.FlightMapFragment;
import org.droidplanner.android.fragments.RCFragment;
import org.droidplanner.android.fragments.TelemetryFragment;
import org.droidplanner.android.fragments.mode.FlightModePanel;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.variables.State;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("deprecation")
public class FlightActivity extends DrawerNavigationUI implements
		FlightActionsFragment.OnMissionControlInteraction, OnDroneListener {

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
	private RCFragment rcFragment;
	private TextView warningView;

	private FlightMapFragment mapFragment;

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
        enableSlidingUpPanel(this.drone);

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

                    // Stop tracking how long this was opened for.
                    GAUtils.sendTiming(new HitBuilders.TimingBuilder()
                            .setCategory(GAUtils.Category.FLIGHT_DATA_DETAILS_PANEL)
                            .setVariable(getString(R.string.ga_mode_details_close_panel))
                            .setValue(System.currentTimeMillis()));
                }
            });

            slidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
                @Override
                public void onDrawerOpened() {
                    final int slidingDrawerWidth = slidingDrawer.getContent().getWidth();
                    final boolean isSlidingDrawerOpened = slidingDrawer.isOpened();
                    updateLocationButtonsMargin(isSlidingDrawerOpened, slidingDrawerWidth);

                    // Track how long this is opened for.
                    GAUtils.sendTiming(new HitBuilders.TimingBuilder()
                            .setCategory(GAUtils.Category.FLIGHT_DATA_DETAILS_PANEL)
                            .setVariable(getString(R.string.ga_mode_details_open_panel))
                            .setValue(System.currentTimeMillis()));
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
                if(mapFragment != null) {
                    mapFragment.updateMapBearing(0);
                }
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

		Fragment flightActions = fragmentManager.findFragmentById(R.id.flightActionsFragment);
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
        Fragment telemetryFragment = fragmentManager.findFragmentById(R.id.telemetryFragment);
        if (telemetryFragment == null) {
            telemetryFragment = new TelemetryFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.telemetryFragment, telemetryFragment)
                    .commit();
        }

        // Add the mode info panel fragment
        Fragment flightModePanel = fragmentManager.findFragmentById(R.id.sliding_drawer_content);
        if (flightModePanel == null) {
            flightModePanel = new FlightModePanel();
            fragmentManager.beginTransaction()
                    .add(R.id.sliding_drawer_content, flightModePanel)
                    .commit();
        }

		DroneshareDialog.perhapsShow(this);
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
	public void onJoystickSelected() {
		toggleRCFragment();
	}

	private void toggleRCFragment() {
		if (rcFragment == null) {
			rcFragment = new RCFragment();
			fragmentManager.beginTransaction().add(R.id.containerRC, rcFragment).commit();
		} else {
			fragmentManager.beginTransaction().remove(rcFragment).commit();
			rcFragment = null;
		}
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
            enableSlidingUpPanel(drone);
            break;

		default:
			break;
		}
	}

    private void enableSlidingUpPanel(Drone drone){
        if (mSlidingPanel == null) {
            return;
        }

        final State droneState = drone.getState();
        final boolean isEnabled = drone.getMavClient().isConnected() && droneState.isArmed()
                && droneState.isFlying();

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

	@Override
	public CharSequence[][] getHelpItems() {
		return new CharSequence[][] { { getString(R.string.help_item_description) },
				{ "https://www.youtube.com/watch?v=btsk7bzn-9Q" } };
	}
}
