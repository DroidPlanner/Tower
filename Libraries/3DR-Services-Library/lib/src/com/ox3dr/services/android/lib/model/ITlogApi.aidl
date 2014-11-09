// ITlogApi.aidl
package com.ox3dr.services.android.lib.model;

import com.ox3dr.services.android.lib.drone.mission.item.raw.GlobalPositionIntMessage;

/**
* Used to access tlog related functionality.
*/
interface ITLogApi {

    /**
    * Loads global position int message from a tlog file.
    */
    GlobalPositionIntMessage[] loadGlobalPositionIntMessages(String filename);

}
