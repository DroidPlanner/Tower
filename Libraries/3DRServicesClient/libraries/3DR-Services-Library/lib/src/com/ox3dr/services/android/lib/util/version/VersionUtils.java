package com.ox3dr.services.android.lib.util.version;

import android.content.Context;

import com.ox3dr.services.android.lib.R;

/**
 * Created by fhuya on 11/12/14.
 */
public class VersionUtils {

    public static int getVersion(Context context){
        return context.getResources().getInteger(R.integer.ox3dr_services_version);
    }
}
