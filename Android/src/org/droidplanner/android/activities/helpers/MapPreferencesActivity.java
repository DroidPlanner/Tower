package org.droidplanner.android.activities.helpers;

import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.EditInputDialog;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.maps.providers.MapProviderPreferences;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import timber.log.Timber;

/**
 * This activity is used to display the preferences for the currently selected
 * map provider. It does so by retrieving the PreferenceFragment instance from
 * the currently selected map provider, and displaying it.
 */
public class MapPreferencesActivity extends FragmentActivity implements EditInputDialog.Listener {

	/**
	 * Bundle key used to pass, and retrieve the current map provider name.
	 */
	public final static String EXTRA_MAP_PROVIDER_NAME = "EXTRA_MAP_PROVIDER_NAME";

	private MapProviderPreferences currentPrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_map_preferences);
		handleIntent(getIntent());
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	/**
	 * Parse the intent used to start the activity, in order to retrieve the
	 * selected map provider name.
	 * 
	 * @param intent
	 *            intent used to start the activity.
	 */
	private void handleIntent(Intent intent) {
		if (intent == null) {
			Timber.w("No intent was used when starting this class.");
			finish();
			return;
		}

		// Retrieve the selected map provider
		final String mapProviderName = intent.getStringExtra(EXTRA_MAP_PROVIDER_NAME);
		final DPMapProvider mapProvider = DPMapProvider.getMapProvider(mapProviderName);
		if (mapProvider == null) {
			Timber.w("Invalid map provider name: " + mapProviderName);
			finish();
			return;
		}

		// Retrieve the current fragment.
		final FragmentManager fm = getFragmentManager();
		currentPrefs = (MapProviderPreferences) fm.findFragmentById(R.id.map_preferences_container);
		if (currentPrefs == null || currentPrefs.getMapProvider() != mapProvider) {
			currentPrefs = mapProvider.getMapProviderPreferences();
			if (currentPrefs == null) {
				Timber.w("Undefined map provider preferences for provider " + mapProviderName);
				finish();
			} else {
				fm.beginTransaction().replace(R.id.map_preferences_container, currentPrefs).commit();
			}
		}
	}

	@Override
	public void onOk(String dialogTag, CharSequence input) {
		if(currentPrefs instanceof EditInputDialog.Listener){
			((EditInputDialog.Listener) currentPrefs).onOk(dialogTag, input);
		}
	}

	@Override
	public void onCancel(String dialogTag) {
		if(currentPrefs instanceof EditInputDialog.Listener){
			((EditInputDialog.Listener) currentPrefs).onCancel(dialogTag);
		}
	}
}
