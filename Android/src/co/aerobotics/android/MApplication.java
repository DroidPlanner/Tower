package co.aerobotics.android;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

/**
 * Created by michaelwootton on 1/29/18.
 */

public class MApplication extends Application {

    private DroidPlannerApp droidPlannerApp;
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        if (droidPlannerApp == null) {
            droidPlannerApp = new DroidPlannerApp();
            droidPlannerApp.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        droidPlannerApp.onCreate();
    }
}
