package org.droidplanner.android.activities.interfaces;

import com.MAVLink.Messages.ardupilotmega.msg_global_position_int;

/**
 * Created by rgayle on 2014-07-04.
 */
public interface OnLocatorListListener {
    void onItemClick(msg_global_position_int message);
}
