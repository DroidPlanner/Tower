package org.droidplanner.android.maps.providers;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceFragment;

/**
 * Parent fragment for the map provider preferences fragment.
 */
public abstract class MapProviderPreferences extends PreferenceFragment {

	/**
	 * @return the map provider this fragment contains preferences for.
	 */
	public abstract DPMapProvider getMapProvider();

	@Override
	public Context getContext() {
		final Activity activity = getActivity();
		if (activity == null)
			return null;

		return activity.getApplicationContext();
	}

}
