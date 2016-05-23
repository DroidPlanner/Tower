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
import org.droidplanner.android.fragments.calibration.imu.FragmentSetupIMU;
import org.droidplanner.android.view.viewPager.TabPageIndicator;

/**
 * Used to calibrate the drone's compass and accelerometer.
 */
public class SensorSetupFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sensor_setup, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final SensorPagerAdapter pagerAdapter = new SensorPagerAdapter(getActivity()
                .getApplicationContext(), getChildFragmentManager());

        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.configuration_pager);
        viewPager.setAdapter(pagerAdapter);

        final TabPageIndicator tabIndicator = (TabPageIndicator) view.findViewById(R.id.configuration_tab_strip);
        tabIndicator.setViewPager(viewPager);
    }

    private static class SensorPagerAdapter extends FragmentPagerAdapter {

        private final Context context;

        public SensorPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new FragmentSetupIMU();

                case 1:
                    return new FragmentSetupCompass();

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return FragmentSetupIMU.getTitle(context);

                case 1:
                    return FragmentSetupCompass.getTitle(context);

                default:
                    return null;
            }
        }
    }
}
