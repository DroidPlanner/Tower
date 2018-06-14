package co.aerobotics.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.activities.LoginActivity;
import co.aerobotics.android.activities.interfaces.APIContract;

import static org.bouncycastle.cms.RecipientId.password;

/**
 * Created by michaelwootton on 6/13/18.
 */

public class Authentication implements APIContract{
    private static final int DRONE_DEMO_ACCOUNT_ID = 247;
    private Context context;
    private MixpanelAPI mixpanelAPI;


    public Authentication(Context context) {
        this.context = context;
        mixpanelAPI = MixpanelAPI.getInstance(context, DroidPlannerApp.getInstance().getMixpanelToken());
    }

    public boolean createUser(String firstName, String lastName, String username, String email, String password) {
        String jsonStr = String.format("{\"email\":\"%s\"," + "\"username\":\"%s\"," +
                        "\"first_name\":\"%s\"," +
                        "\"last_name\":\"%s\"," + "\"password\":\"%s\"," + "\"from_app\":\"flight\"," + "\"app\":\"flight\"}",
                email,
                username,
                firstName,
                lastName,
                password);
        PostRequest postRequest = new PostRequest();
        postRequest.login(jsonStr, APIContract.GATEWAY_USERS);

        do {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!postRequest.isServerResponseReceived());

        if (postRequest.isServerError()){
            //TODO handle error message better
            setResultToToast("Error: Email address already exists for a user");
            return false;
        }
        mixpanelAPI.identify(email);
        mixpanelAPI.getPeople().identify(email);
        mixpanelAPI.getPeople().set("Email", email);
        mixpanelAPI.track("FPA: UserSignUpSuccess", null);
        mixpanelAPI.flush();
        return true;
    }

    public boolean authenticateUser(String mEmail, String mPassword) {
        String url = APIContract.USER_AUTH_GET_TOKEN;
        String jsonStr = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", mEmail, mPassword);
        PostRequest postRequest = new PostRequest();
        postRequest.login(jsonStr, url);

        do {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!postRequest.isServerResponseReceived());

        if (postRequest.isServerError()){
            return false;
        }

        SQLiteDatabaseHandler sqLiteDatabaseHandler = new SQLiteDatabaseHandler(context);
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.com_dji_android_PREF_FILE_KEY), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        try {
            editor.putBoolean(context.getString(R.string.logged_in), true);
            editor.putString(context.getString(R.string.user_auth_token), postRequest.getResponseData().getString("token"));
            JSONObject user = postRequest.getResponseData().getJSONObject("user");
            int userId = user.getInt("id");
            JSONArray clients = user.getJSONArray("clients");
            int activeClientId = -1;
            List<Integer> allClientsIds = new ArrayList<>();
            for (int i = 0; i < clients.length(); i++) {
                JSONObject client = clients.getJSONObject(i);
                int clientUserId = client.getInt("user_id");
                int clientId = client.getInt("id");
                // if not drone demo account
                if (clientId != DRONE_DEMO_ACCOUNT_ID) {
                    allClientsIds.add(clientId);
                    JSONArray farmsArray = client.getJSONArray("farms");
                    List<Integer> farmIdsFromServer = new ArrayList<>();
                    for (int j = 0; j < farmsArray.length(); j++) {
                        JSONObject dict = farmsArray.getJSONObject(j);
                        farmIdsFromServer.add(dict.getInt("id"));
                        sqLiteDatabaseHandler.createFarmName(dict.getString("name"),
                                dict.getInt("id"), clientId);
                    }
                    String allClientIds = allClientsIds.toString().replaceAll("\\[", "").replaceAll("]","");
                    List<JSONObject> farmJsonList = sqLiteDatabaseHandler.getFarmNamesAndIdList(allClientIds);
                    List<Integer> localFarmIds = new ArrayList<>();
                    for (int k = 0; k < farmJsonList.size(); k++) {
                        JSONObject farm = farmJsonList.get(k);
                        Integer farmId = farm.getInt("farm_id");
                        localFarmIds.add(farmId);
                    }

                    for (Integer farmId: localFarmIds) {
                        if (!farmIdsFromServer.contains(farmId)) {
                            sqLiteDatabaseHandler.deleteFarm(farmId);
                            sqLiteDatabaseHandler.deleteAllBoundariesThatBelongToFarm(farmId);
                        }
                    }
                }
                if (userId == clientUserId) {
                    activeClientId = clientId;
                }
            }
            editor.putString(context.getString(R.string.all_client_ids), new Gson().toJson(allClientsIds));
            editor.putInt(context.getString(R.string.client_id), activeClientId);
            editor.apply();

            mixpanelAPI.identify(mEmail);
            mixpanelAPI.getPeople().identify(mEmail);
            mixpanelAPI.getPeople().set("Email", mEmail);
            mixpanelAPI.track("FPA: UserLoginSuccess", null);
            mixpanelAPI.flush();
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setResultToToast(final String string) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, string, Toast.LENGTH_LONG).show();
            }
        });
    }
}
