package com.o3dr.android.client.interfaces;

import android.os.Bundle;

/**
 * Created by fhuya on 11/18/14.
 */
public interface DroneListener {

    void onDroneEvent(String event, Bundle extras);

    void onDroneServiceInterrupted(String errorMsg);
}
