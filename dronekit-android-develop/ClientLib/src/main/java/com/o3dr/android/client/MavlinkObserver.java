package com.o3dr.android.client;

import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.IMavlinkObserver;

/**
 * Allows to register for mavlink message updates.
 */
public abstract class MavlinkObserver extends IMavlinkObserver.Stub {

    @Override
    public abstract void onMavlinkMessageReceived(MavlinkMessageWrapper mavlinkMessageWrapper);
}
