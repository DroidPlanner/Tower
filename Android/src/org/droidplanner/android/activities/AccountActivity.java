package org.droidplanner.android.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.DroneshareLoginFragment;

/**
 * Created by Fredia Huya-Kouadio on 1/22/15.
 */
public class AccountActivity extends DrawerNavigationUI {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        if(savedInstanceState == null){
            final FragmentManager fm = getSupportFragmentManager();
            Fragment droneShare = fm.findFragmentById(R.id.fragment_droneshare_account);
            if(droneShare == null){
//                if(mAppPrefs.isDroneshareEnabled()){
//                    droneShare = new DroneshareAccountFragment();
//                }
//                else
                    droneShare = new DroneshareLoginFragment();

                fm.beginTransaction().add(R.id.fragment_droneshare_account, droneShare).commit();
            }
        }
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
