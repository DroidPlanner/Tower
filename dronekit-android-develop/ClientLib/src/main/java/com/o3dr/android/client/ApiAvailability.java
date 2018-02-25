package com.o3dr.android.client;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.o3dr.services.android.lib.model.IDroidPlannerServices;
import com.o3dr.services.android.lib.util.version.VersionUtils;

import org.droidplanner.services.android.impl.api.DroidPlannerService;

import java.util.List;

/**
 * Helper class to verify that the DroneKit-Android services APK is available and up-to-date
 * Created by Fredia Huya-Kouadio on 7/7/15.
 */
class ApiAvailability {

    private static class LazyHolder {
        private static final ApiAvailability INSTANCE = new ApiAvailability();
    }

    private static final String SERVICES_CLAZZ_NAME = IDroidPlannerServices.class.getName();
    private static final String METADATA_KEY = "com.o3dr.dronekit.android.core.version";

    private static final int INVALID_LIB_VERSION = -1;

    //Private to prevent instantiation
    private ApiAvailability() {
    }

    static ApiAvailability getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * Find and returns the most adequate instance of the services lib.
     *
     * @param context Application context. Must not be null.
     * @return intent Intent used to bind to an instance of the services lib.
     */
    Intent getAvailableServicesInstance(@NonNull final Context context) {
        final PackageManager pm = context.getPackageManager();

        //Check if an instance of the services library is up and running.
        final Intent serviceIntent = new Intent(SERVICES_CLAZZ_NAME);
        final List<ResolveInfo> serviceInfos = pm.queryIntentServices(serviceIntent, PackageManager.GET_META_DATA);
        if(serviceInfos != null && !serviceInfos.isEmpty()){
            for(ResolveInfo serviceInfo : serviceInfos) {
                final Bundle metaData = serviceInfo.serviceInfo.metaData;
                if (metaData == null)
                    continue;

                final int coreLibVersion = metaData.getInt(METADATA_KEY, INVALID_LIB_VERSION);
                if (coreLibVersion != INVALID_LIB_VERSION && coreLibVersion >= VersionUtils.getCoreLibVersion(context)) {
                    serviceIntent.setClassName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name);
                    return serviceIntent;
                }
            }
        }

        //Didn't find any that's up and running. Enable the local one
        DroidPlannerService.enableDroidPlannerService(context, true);
        serviceIntent.setClass(context, DroidPlannerService.class);
        return serviceIntent;
    }

}
