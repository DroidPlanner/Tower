package co.aerobotics.android.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.activities.interfaces.APIContract;

/**
 * Created by michaelwootton on 6/13/18.
 */

public class Authentication implements APIContract{
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

    private void setResultToToast(final String string) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, string, Toast.LENGTH_LONG).show();
            }
        });
    }
}
