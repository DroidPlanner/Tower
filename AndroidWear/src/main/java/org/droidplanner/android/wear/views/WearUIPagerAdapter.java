package org.droidplanner.android.wear.views;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.wearable.view.FragmentGridPagerAdapter;

import org.droidplanner.android.wear.fragments.WearFlightActionsFragment;
import org.droidplanner.android.wear.fragments.WearMapFragment;

/**
 * Fragment pager adapter for the droidplanner wear UI.
 */
public class WearUIPagerAdapter extends FragmentGridPagerAdapter {

    public WearUIPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getFragment(int i, int i2) {
        return new WearFlightActionsFragment();
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int i) {
        return 1;
    }

    @Override
    public long getFragmentId(int row, int column){
        return 0l;
    }
}
