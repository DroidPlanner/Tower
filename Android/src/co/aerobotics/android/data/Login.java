package co.aerobotics.android.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.activities.interfaces.APIContract;

/**
 * Created by michaelwootton on 6/15/18.
 */

public class Login {

    private final Context context;
    private MixpanelAPI mixpanelAPI;
    private String mEmail;
    private String mPassword;
    private SharedPreferences sharedPref;
    private PostRequest postRequest = new PostRequest();

    public Login (Context context, String mEmail, String mPassword) {
        this.context = context;
        mixpanelAPI = MixpanelAPI.getInstance(context, DroidPlannerApp.getInstance().getMixpanelToken());
        this.mEmail = mEmail;
        this.mPassword = mPassword;
        sharedPref = context.getSharedPreferences(context.getString(R.string.com_dji_android_PREF_FILE_KEY), Context.MODE_PRIVATE);
    }

    public boolean authenticateUser() {

        if (!makePostRequest()) {
            return false;
        }
        JSONObject returnData = postRequest.getResponseData();
        try {
            JSONObject user = returnData.getJSONObject("user");
            FarmDataHandler farmDataHandler = new FarmDataHandler(context, user);
            farmDataHandler.parseUsersFarms();
            // userId = user.getInt("id");
            // parseUsersFarms(user);
            String authToken = returnData.getString("token");
            writeValuesToSharedPrefs(farmDataHandler.getAllClientsIds(), authToken, farmDataHandler.getActiveClientId(), farmDataHandler.getUserId());
        } catch (JSONException e) {
            return false;
        }
        setupMixpanel();
        return true;
    }

    private void writeValuesToSharedPrefs(List<Integer> allClientsIds, String authToken, Integer activeClientId, Integer userId) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.logged_in), true);
        editor.putString(context.getString(R.string.user_auth_token), authToken);
        editor.putString(context.getString(R.string.all_client_ids), new Gson().toJson(allClientsIds));
        editor.putInt(context.getString(R.string.client_id), activeClientId);
        editor.putInt(context.getString(R.string.user_id), userId);
        editor.apply();
    }

    private boolean makePostRequest() {
        String url = APIContract.USER_AUTH_GET_TOKEN;
        String jsonStr = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", mEmail, mPassword);
        postRequest.login(jsonStr, url);
        do {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!postRequest.isServerResponseReceived());

        return !postRequest.isServerError();
    }

    private void setupMixpanel() {
        mixpanelAPI.identify(mEmail);
        mixpanelAPI.getPeople().identify(mEmail);
        mixpanelAPI.getPeople().set("Email", mEmail);
        mixpanelAPI.track("FPA: UserLoginSuccess", null);
        mixpanelAPI.flush();
    }
}
