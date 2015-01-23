package org.droidplanner.android.activities;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.geeksville.apiproxy.LoginFailedException;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.interfaces.AccountLoginListener;
import org.droidplanner.android.fragments.account.DroneshareAccountFragment;
import org.droidplanner.android.fragments.account.DroneshareLoginFragment;
import org.droidplanner.android.utils.connection.DroneshareClient;

/**
 * Created by Fredia Huya-Kouadio on 1/22/15.
 */
public class AccountActivity extends DrawerNavigationUI implements AccountLoginListener {

    private final static String TAG = AccountActivity.class.getSimpleName();

    private final DroneshareClient dshareClient = new DroneshareClient();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        if (savedInstanceState == null) {
            new LoginChecker().execute();
        }
    }

    @Override
    public void onSuccessfulLogin() {
        Fragment currentFragment = getCurrentFragment();
        if (!(currentFragment instanceof DroneshareAccountFragment)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_droneshare_account, new DroneshareAccountFragment())
                    .commit();
        }
    }

    @Override
    public void onFailedLogin() {

    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_droneshare_account);
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_toolbar;
    }

    @Override
    protected int getNavigationDrawerEntryId() {
        return R.id.navigation_account;
    }

    private class LoginChecker extends AsyncTask<Void, Void, Fragment> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute(){
            progressDialog = new ProgressDialog(AccountActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(true);
            progressDialog.setTitle("Checking logging credentials...");
            progressDialog.show();
        }

        @Override
        protected void onCancelled(){
            progressDialog.dismiss();
        }

        @Override
        protected Fragment doInBackground(Void... params) {
            Fragment droneShare = new DroneshareLoginFragment();

            if (mAppPrefs.isDroneshareEnabled()) {
                //Check if the credentials are valid.
                final String username = mAppPrefs.getDroneshareLogin();
                final String password = mAppPrefs.getDronesharePassword();

                try {
                    if (dshareClient.login(username, password))
                        droneShare = new DroneshareAccountFragment();
                } catch (LoginFailedException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }

            return droneShare;
        }

        @Override
        protected void onPostExecute(Fragment fragment) {
            progressDialog.dismiss();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_droneshare_account, fragment).commit();
        }
    }
}
