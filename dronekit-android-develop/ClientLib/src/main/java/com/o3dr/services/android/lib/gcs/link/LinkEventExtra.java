package com.o3dr.services.android.lib.gcs.link;

/**
 * Holds handles used to retrieve additional information broadcast along a link event.
 *
 * @see {@link LinkEvent}
 */
public class LinkEventExtra {
    private LinkEventExtra() {
    }

    private static final String PACKAGE_NAME = "com.o3dr.services.android.lib.gcs.link.event.extra";

    /**
     * Used to access the link connection status.
     *
     * @see {@link LinkConnectionStatus}
     * @see {@link com.o3dr.services.android.lib.gcs.link.LinkEvent#LINK_STATE_UPDATED}
     */
    public static final String EXTRA_CONNECTION_STATUS = PACKAGE_NAME + ".CONNECTION_STATUS";
}
