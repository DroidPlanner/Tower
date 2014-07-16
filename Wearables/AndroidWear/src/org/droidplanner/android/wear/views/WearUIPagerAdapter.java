package org.droidplanner.android.wear.views;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.wearable.view.FragmentGridPagerAdapter;

import org.droidplanner.android.wear.fragments.SetFlightModeFragment;
import org.droidplanner.android.wear.fragments.ToggleConnectionFragment;
import org.droidplanner.android.wear.fragments.ToggleFollowMeFragment;

/**
 * Fragment pager adapter for the droidplanner wear UI.
 */
public class WearUIPagerAdapter extends FragmentGridPagerAdapter {

    public WearUIPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getFragment(int row, int column) {
        switch(column){
            case 0:
            default:
                return new ToggleConnectionFragment();

            case 1:
                return new SetFlightModeFragment();

            case 2:
                return new ToggleFollowMeFragment();
        }
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int i) {
        return 3;
    }

    @Override
    public long getFragmentId(int row, int column){
        return column;
    }
}
