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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        if (savedInstanceState == null) {
            Fragment droneShare;
            if(mAppPrefs.isDroneshareEnabled())
                droneShare = new DroneshareAccountFragment();
            else
                droneShare = new DroneshareLoginFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.droneshare_account, droneShare).commit();
        }
    }

    @Override
    public void onLogin() {
        Fragment currentFragment = getCurrentFragment();
        if (!(currentFragment instanceof DroneshareAccountFragment)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.droneshare_account, new DroneshareAccountFragment())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onFailedLogin() {

    }

    @Override
    public void onLogout() {
        Fragment currentFragment = getCurrentFragment();
        if (!(currentFragment instanceof DroneshareLoginFragment)) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.droneshare_account, new DroneshareLoginFragment())
                    .commitAllowingStateLoss();
        }
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.droneshare_account);
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_toolbar;
    }

    @Override
    protected int getNavigationDrawerEntryId() {
        return R.id.navigation_account;
    }
}
