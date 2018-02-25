// IMavlinkObserver.aidl
package com.o3dr.services.android.lib.model;

import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;

/**
* Asynchronous notification on receipt of new mavlink message.
*/
oneway interface IMavlinkObserver {

    /**
    * Notify observer that a mavlink message was received.
    * @param messageWrapper Wrapper for the received mavlink message.
    */
    void onMavlinkMessageReceived(in MavlinkMessageWrapper messageWrapper);
}
