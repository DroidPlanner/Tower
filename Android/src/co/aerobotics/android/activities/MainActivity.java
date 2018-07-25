package co.aerobotics.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.activities.interfaces.APIContract;
import co.aerobotics.android.data.PostRequest;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPref;
    MixpanelAPI mMixpanel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMixpanel = MixpanelAPI.getInstance(this, DroidPlannerApp.getInstance().getMixpanelToken());
        mMixpanel.track("FPA: AppLaunched");
        sharedPref = MainActivity.this.getSharedPreferences(getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        boolean isFirstLaunch = sharedPref.getBoolean("firstLaunch", true);
        boolean loggedIn = sharedPref.getBoolean(getString(R.string.logged_in), false);
        if (isFirstLaunch){
            sharedPref.edit().putBoolean("firstLaunch", false).apply();
            navigateToActivity(IntroActivity.class);
        } else if (loggedIn){
            String token = getAuthToken();
            if (tokenExistsOnDevice(token)) {
                clearSharedPrefs();
                navigateToActivity(LoginActivity.class);
            } else {
                if (networkIsAvailable()) {
                    CheckTokenTask checkTokenTask = new CheckTokenTask(token);
                    checkTokenTask.execute();
                } else {
                    Integer userId = getUserId();
                    setMixpanelUser(userId);
                    navigateToActivity(EditorActivity.class);
                }
            }
        } else {
            navigateToActivity(LoginActivity.class);
        }
    }

    private Integer getUserId() {
      return sharedPref.getInt(getString(R.string.user_id), -1);
    }

    private void setMixpanelUser(Integer userId) {
        mMixpanel.identify(userId.toString());
        mMixpanel.getPeople().identify(userId.toString());
        mMixpanel.getPeople().set("UserId", userId.toString());
    }
    private void clearSharedPrefs() {
        sharedPref.edit().clear().apply();
    }

    private boolean tokenExistsOnDevice(String token) {
       return Objects.equals(token, "");
    }

    private String getAuthToken() {
        return sharedPref.getString(getString(R.string.user_auth_token), "");
    }

    private void navigateToActivity(Class activity) {
        Intent intent = new Intent(MainActivity.this, activity);
        MainActivity.this.startActivity(intent);
        finish();
    }

    private boolean networkIsAvailable() {
        return DroidPlannerApp.getInstance().isNetworkAvailable();
    }

    public boolean checkTokenExistsOnServer(String token) {
        PostRequest postRequest = new PostRequest();
        postRequest.get(APIContract.GATEWAY_CONFIRM_TOKEN, token);
        do {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!postRequest.isServerResponseReceived());

        return !postRequest.isServerError();
    }

    public class CheckTokenTask extends AsyncTask<Void, Void, Boolean> implements APIContract {

        private final String token;

        CheckTokenTask(String token) {
            this.token = token;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return checkTokenExistsOnServer(token);
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                Integer userId = getUserId();
                setMixpanelUser(userId);
                navigateToActivity(EditorActivity.class);
            } else {
                clearSharedPrefs();
                navigateToActivity(LoginActivity.class);
            }
        }
    }
}
