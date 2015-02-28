package org.droidplanner.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.calibration.rc.FragmentSetupGC;
import org.droidplanner.android.fragments.calibration.rc.FragmentSetupRC;
import org.droidplanner.android.widgets.viewPager.TabPageIndicator;

/**
 * Used for rc control limits and setup physical joystick, accelerometer and touch control
 */
public class RcControlSetupFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_sensor_setup, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        final RCPagerAdapter pagerAdapter = new RCPagerAdapter(getActivity()
                .getApplicationContext(), getChildFragmentManager());

        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.configuration_pager);
        viewPager.setAdapter(pagerAdapter);

        final TabPageIndicator tabIndicator = (TabPageIndicator) view.findViewById(R.id
                .configuration_tab_strip);
        tabIndicator.setViewPager(viewPager);
    }

    private static class RCPagerAdapter extends FragmentPagerAdapter {

        private final Context context;

        public RCPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int i) {
            switch(i){
                case 0:
                default:
                    return new FragmentSetupGC();
                case 1:
                    return new FragmentSetupRC();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position){
            switch(position){
                case 0:
                default:
                	return FragmentSetupGC.getTitle(context);
                case 1:
                	return FragmentSetupRC.getTitle(context);
            }
        }
    }
}
