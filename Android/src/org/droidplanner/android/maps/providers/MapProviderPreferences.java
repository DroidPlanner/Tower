package org.droidplanner.android.maps.providers;

import android.preference.PreferenceFragment;

/**
 * Parent fragment for the map provider preferences fragment.
 */
public abstract class MapProviderPreferences extends PreferenceFragment {

    /**
     * @return the map provider this fragment contains preferences for.
     */
    public abstract DPMapProvider getMapProvider();
}
