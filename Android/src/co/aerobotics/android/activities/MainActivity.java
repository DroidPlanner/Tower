package co.aerobotics.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import com.mixpanel.android.mpmetrics.MixpanelAPI;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MixpanelAPI mMixpanel = MixpanelAPI.getInstance(this, DroidPlannerApp.getInstance().getMixpanelToken());
        mMixpanel.track("FPA: AppLaunched");

        SharedPreferences sharedPref = MainActivity.this.getSharedPreferences(getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        boolean isFirstLaunch = sharedPref.getBoolean("firstLaunch", true);
        boolean loggedIn = sharedPref.getBoolean(getString(R.string.logged_in), false);
        final Intent intent;
        if (isFirstLaunch){
            sharedPref.edit().putBoolean("firstLaunch", false).apply();
            intent = new Intent(MainActivity.this, IntroActivity.class);
        } else if (loggedIn){
            String mEmail = sharedPref.getString(getString(R.string.username), "");
            mMixpanel.identify(mEmail);
            mMixpanel.getPeople().identify(mEmail);
            mMixpanel.getPeople().set("Email", mEmail);
            intent = new Intent(MainActivity.this, EditorActivity.class);
        } else{
            intent = new Intent(MainActivity.this, LoginActivity.class);
        }
        MainActivity.this.startActivity(intent);
        finish();
    }
}
