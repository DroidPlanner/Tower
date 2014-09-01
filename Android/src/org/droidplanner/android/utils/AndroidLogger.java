package org.droidplanner.android.utils;

import org.droidplanner.core.model.Logger;

/**
 * Android specific implementation for the {org.droidplanner.core.model.Logger} interface.
 */
public class AndroidLogger implements Logger {
    private static Logger sLogger = new AndroidLogger();

    public static Logger getLogger() {
        return sLogger;
    }

    //Only one instance is allowed.
    private AndroidLogger(){}

    @Override
    public void logVerbose(String logTag, String verbose) {

    }

    @Override
    public void logDebug(String logTag, String debug) {

    }

    @Override
    public void logInfo(String logTag, String info) {

    }

    @Override
    public void logWarning(String logTag, String warning) {

    }

    @Override
    public void logWarning(String logTag, Exception exception) {

    }

    @Override
    public void logWarning(String logTag, String warning, Exception exception) {

    }

    @Override
    public void logErr(String logTag, String err) {

    }

    @Override
    public void logErr(String logTag, Exception exception) {

    }

    @Override
    public void logErr(String logTag, String err, Exception exception) {

    }
}
