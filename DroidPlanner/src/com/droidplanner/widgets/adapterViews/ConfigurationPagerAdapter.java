package com.droidplanner.widgets.adapterViews;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;
import com.droidplanner.fragments.ParametersTableFragment;
import com.droidplanner.fragments.RcSetupFragment;
import com.droidplanner.fragments.SettingsFragment;
import com.droidplanner.fragments.TuningFragment;

/**
 * This is the Configuration Activity pager adapter.
 * @author fhuya
 * @since 1.2.0
 */
public class ConfigurationPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = ConfigurationPagerAdapter.class.getName();
    private final Context mContext;

    public ConfigurationPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment tabFragment = null;
        switch(i){
            case 0:
                tabFragment = new TuningFragment();
                break;

            case 1:
                tabFragment = new RcSetupFragment();
                break;

            case 2:
                tabFragment = new ParametersTableFragment();
                break;

            case 3:
                tabFragment = new SettingsFragment();
                break;

            default:
                Log.e(TAG, "Invalid index for view pager item (#" + i + ")");
                break;
        }
        return tabFragment;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position){
        CharSequence title = null;
        switch(position){
            case 0:
                title = mContext.getString(TuningFragment.LABEL_RESOURCE);
                break;

            case 1:
                title= mContext.getString(RcSetupFragment.LABEL_RESOURCE);
                break;

            case 2:
                title = mContext.getString(ParametersTableFragment.LABEL_RESOURCE);
                break;

            case 3:
                title = mContext.getString(SettingsFragment.LABEL_RESOURCE);
                break;

            default:
                Log.e(TAG, "Invalid index for view pager item title (#" + position + ")");
                break;
        }
        return title;
    }
}
