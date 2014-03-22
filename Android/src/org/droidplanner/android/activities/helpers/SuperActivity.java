package org.droidplanner.android.activities.helpers;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.dialogs.AltitudeDialog;
import org.droidplanner.android.dialogs.AltitudeDialog.OnAltitudeChangedListener;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.android.fragments.helpers.BTDeviceListFragment;
import org.droidplanner.android.fragments.helpers.OfflineMapFragment;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.android.utils.Constants;
import org.droidplanner.android.utils.Utils;

import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

public abstract class SuperActivity extends FragmentActivity implements OnAltitudeChangedListener {

	public DroidPlannerApp app;
	public Drone drone;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		app = (DroidPlannerApp) getApplication();
		this.drone = app.drone;

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
            toggleDroneConnection();
			return true;

		case R.id.menu_map_type_hybrid:
		case R.id.menu_map_type_normal:
		case R.id.menu_map_type_terrain:
		case R.id.menu_map_type_satellite:
			setMapTypeFromItemId(item.getItemId());
			return true;

            default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

    protected void toggleDroneConnection(){
        if (!drone.MavClient.isConnected()) {
            final String connectionType = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext())
                    .getString(Constants.PREF_CONNECTION_TYPE,
                            Constants.DEFAULT_CONNECTION_TYPE);

            if (Utils.ConnectionType.BLUETOOTH.name().equals(connectionType)) {
                //Launch a bluetooth device selection screen for the user
                new BTDeviceListFragment().show(getSupportFragmentManager(), "Device selection dialog");
                return;
            }
        }
        drone.MavClient.toggleConnectionState();
    }

	private void setMapTypeFromItemId(int itemId) {
		final String mapType;
		switch (itemId) {
		case R.id.menu_map_type_hybrid:
			mapType = OfflineMapFragment.MAP_TYPE_HYBRID;
			break;
		case R.id.menu_map_type_normal:
			mapType = OfflineMapFragment.MAP_TYPE_NORMAL;
			break;
		case R.id.menu_map_type_terrain:
			mapType = OfflineMapFragment.MAP_TYPE_TERRAIN;
			break;
		default:
			mapType = OfflineMapFragment.MAP_TYPE_SATELLITE;
			break;
		}

		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putString(OfflineMapFragment.PREF_MAP_TYPE, mapType).commit();

		//drone.notifyMapTypeChanged();
	}

	public void changeDefaultAlt() {
		AltitudeDialog dialog = new AltitudeDialog(this);
		dialog.build(drone.mission.getDefaultAlt(), this);
	}

	@Override
	public void onAltitudeChanged(Altitude newAltitude) {
		drone.mission.setDefaultAlt(newAltitude);
	}
}
