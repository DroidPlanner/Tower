package org.droidplanner.android.activities.helpers;

import org.droidplanner.R;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.YesNoWithPrefsDialog;
import org.droidplanner.android.helpers.ApiInterface;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.api.services.DroidPlannerService;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.widgets.actionProviders.InfoBarActionProvider;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Parent class for the app activity classes.
 */
public abstract class SuperUI extends FragmentActivity implements OnDroneListener,
        ApiInterface.Provider, ApiInterface.Subscriber {

    private final ServiceConnection dpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            setApiHandle(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unsetApiHandle();
        }
    };

    private boolean wasApiConnectedCalled;
    private boolean wasApiDisconnectedCalled;

    private ScreenOrientation screenOrientation = new ScreenOrientation(this);
	protected DroidPlannerApi dpApi;

	/**
	 * Handle to the app preferences.
	 */
	protected DroidPlannerPrefs mAppPrefs;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
		}

		mAppPrefs = new DroidPlannerPrefs(getApplicationContext());

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		/*
		 * Used to supplant wake lock acquisition (previously in
		 * org.droidplanner.android.service .MAVLinkService) as suggested by the
		 * android android.os.PowerManager#newWakeLock documentation.
		 */
		if (mAppPrefs.keepScreenOn()) {
			getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		screenOrientation.unlock();
		Utils.updateUILanguage(getApplicationContext());

        bindService(new Intent(getApplicationContext(), DroidPlannerService.class),
                dpServiceConnection, Context.BIND_AUTO_CREATE);
	}

    @Override
    public void onDestroy(){
        super.onDestroy();
        unsetApiHandle();
        unbindService(dpServiceConnection);
    }

    private void setApiHandle(IBinder service){
        wasApiConnectedCalled = false;
        onApiConnected((DroidPlannerApi) service);
        if(!wasApiConnectedCalled){
            throw new IllegalStateException("super.onApiConnected() was not " +
                    "called.");
        }
    }

    public void unsetApiHandle(){
        wasApiDisconnectedCalled = false;
        onApiDisconnected();
        if(!wasApiDisconnectedCalled){
            throw new IllegalStateException("super.onApiDisconnected() was not " +
                    "called");
        }
    }

    @Override
    public final DroidPlannerApi getApi(){
        return dpApi;
    }

    @Override
    public void onApiConnected(DroidPlannerApi api){
        dpApi = api;

        invalidateOptionsMenu();

        api.addDroneListener(SuperUI.this);
        api.getMavClient().queryConnectionState();
        api.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);

        wasApiConnectedCalled = true;
    }

    @Override
    public void onApiDisconnected(){
        if(dpApi != null) {
                dpApi.removeDroneListener(this);
        }

        dpApi = null;

        wasApiDisconnectedCalled = true;
    }



	@Override
	protected void onStart() {
		super.onStart();
		maxVolumeIfEnabled();
	}

	private void maxVolumeIfEnabled() {
		if (mAppPrefs.maxVolumeOnStart()) {
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		}
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case CONNECTED:
			invalidateOptionsMenu();
			screenOrientation.requestLock();
			break;

		case DISCONNECTED:
			invalidateOptionsMenu();
			screenOrientation.unlock();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_super_activiy, menu);

		final MenuItem toggleConnectionItem = menu.findItem(R.id.menu_connect);

		// Configure the info bar action provider if we're connected
		if (dpApi != null && dpApi.isConnected()) {
			menu.setGroupEnabled(R.id.menu_group_connected, true);
			menu.setGroupVisible(R.id.menu_group_connected, true);

			toggleConnectionItem.setTitle(R.string.menu_disconnect);

		} else {
			menu.setGroupEnabled(R.id.menu_group_connected, false);
			menu.setGroupVisible(R.id.menu_group_connected, false);

			toggleConnectionItem.setTitle(R.string.menu_connect);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_load_mission:
            if(dpApi != null) {
                dpApi.getWaypointManager().getWaypoints();
            }
			return true;

		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			toggleDroneConnection();
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	public void toggleDroneConnection() {
        startService(new Intent(getApplicationContext(), DroidPlannerService.class).setAction
                (DroidPlannerService.ACTION_TOGGLE_DRONE_CONNECTION));
	}

}