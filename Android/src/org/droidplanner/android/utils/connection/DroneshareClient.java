package org.droidplanner.android.utils.connection;

import android.util.Log;

import com.geeksville.apiproxy.GCSHookImpl;
import com.geeksville.apiproxy.LoginFailedException;

import java.io.IOException;

/**
 * Created by Fredia Huya-Kouadio on 1/22/15.
 */
public class DroneshareClient extends GCSHookImpl {

    private static final String TAG = DroneshareClient.class.getSimpleName();

    public static final int SIGNUP_SUCCESSFUL = 0;
    public static final int SIGNUP_FAILED = 1;
    public static final int SIGNUP_USERNAME_NOT_AVAILABLE = 2;

    public boolean login(String login, String password) throws LoginFailedException {
        try {
            super.connect();

            loginUser(login, password);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to connect due to " + e, e);
            if(e instanceof LoginFailedException)
                throw (LoginFailedException) e;
        }
        finally{
            try {
                super.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close connection due to " + e, e);
            }
        }

        return false;
    }

    public int signupUser(String login, String password, String email) throws LoginFailedException{
        try{
            super.connect();
            if(isUsernameAvailable(login)) {
                createUser(login, password, email);
                return SIGNUP_SUCCESSFUL;
            }
            return SIGNUP_USERNAME_NOT_AVAILABLE;
        }catch(Exception e){
            Log.e(TAG, "Failed to sign up due to " + e, e);
            if(e instanceof LoginFailedException)
                throw (LoginFailedException) e;
        }
        finally{
            try {
                super.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close connection due to " + e, e);
            }
        }

        return SIGNUP_FAILED;
    }
}
