package org.droidplanner.android.utils.analytics;

/**
 * List the analytics categories used in the app.
 */
public enum GATrackerCategory {
    FLIGHT_DATA_DETAILS_PANEL,
    FLIGHT_DATA_ACTION_BUTTON;

    @Override
    public String toString(){
        return name().toLowerCase();
    }
}
