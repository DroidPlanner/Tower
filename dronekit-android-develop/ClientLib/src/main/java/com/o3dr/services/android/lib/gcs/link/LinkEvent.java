package com.o3dr.services.android.lib.gcs.link;

/**
 * Stores all possible link events.
 */
public class LinkEvent {

    private LinkEvent() {
    }

    private static final String PACKAGE_NAME = "com.o3dr.services.android.lib.gcs.link.event";

    /**
     * Notifies what the link connection status currently is.
     *
     * @see {@link LinkEventExtra#EXTRA_CONNECTION_STATUS}
     */
    public static final String LINK_STATE_UPDATED = PACKAGE_NAME + ".LINK_STATE_UPDATED";

}
