package org.droidplanner.sample.hellodrone;

import android.app.Application;

import com.o3dr.android.client.utils.LogToFileTree;

import timber.log.Timber;

/**
 * Created by fredia on 5/20/16.
 */
public class StarterApplication extends Application {

    /**
     * Used to channel logging
     */
    private LogToFileTree logToFileTree;

    @Override
    public void onCreate(){
        super.onCreate();

        if (com.o3dr.android.client.BuildConfig.DEBUG) {
            logToFileTree = new LogToFileTree();
            Timber.plant(logToFileTree);
        }

        createFileStartLogging();
    }

    private void createFileStartLogging() {
        if (logToFileTree != null) {
            logToFileTree.createFileStartLogging(getApplicationContext());
        }
    }

    private void closeLogFile() {
        if(logToFileTree != null) {
            logToFileTree.stopLoggingThread();
        }
    }
}
