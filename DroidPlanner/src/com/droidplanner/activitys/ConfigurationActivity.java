package com.droidplanner.activitys;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperUI;
import com.droidplanner.fragments.ChecklistFragment;
import com.droidplanner.fragments.ModesSetupFragment;
import com.droidplanner.fragments.ParamsFragment;
import com.droidplanner.fragments.RcSetupFragment;
import com.droidplanner.fragments.SettingsFragment;
import com.droidplanner.fragments.TuningFragment;
import com.droidplanner.widgets.viewPager.TabPageIndicator;


public class ConfigurationActivity extends SuperUI{

	public static final String SCREEN_INTENT = "screen";
    public static final String SETTINGS = "settings";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuration);

        final ConfigurationPagerAdapter pagerAdapter = new ConfigurationPagerAdapter
                (getApplicationContext(), getFragmentManager());

        final ViewPager viewPager = (ViewPager) findViewById(R.id.configuration_pager);
        viewPager.setAdapter(pagerAdapter);

        final TabPageIndicator tabIndicator = (TabPageIndicator)findViewById(R.id
                .configuration_tab_strip);
        tabIndicator.setViewPager(viewPager);

		final ActionBar actionBar = getActionBar();
        if(actionBar != null){
		    actionBar.setDisplayHomeAsUpEnabled(true);
        }

	    Intent intent = getIntent();
	    String stringExtra = intent.getStringExtra(SCREEN_INTENT);
		if(SETTINGS.equalsIgnoreCase(stringExtra)){
	    	viewPager.setCurrentItem(0);
	    }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    /**
     * This is the fragment pager adapter to handle the tabs of the Configuration activity.
     * @since 1.2.0
     */
    private static class ConfigurationPagerAdapter extends FragmentPagerAdapter {

        /**
         * Application context object used to retrieve the tabs' title.
         * @since 1.2.0
         */
        private final Context mContext;

        public ConfigurationPagerAdapter(Context context, FragmentManager fm){
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position){
            switch(position){
                case 0:
                    return new SettingsFragment();

                case 1:
                    return new TuningFragment();

                case 2:
                    return new RcSetupFragment();

                case 3:
                    return new ModesSetupFragment();

                case 4:
                    return new ChecklistFragment();

                case 5:
                    return new ParamsFragment();

                default:
                    return null;
            }
        }

        @Override
        public int getCount(){
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position){
            switch(position){
                case 0:
                    return mContext.getText(R.string.settings);

                case 1:
                    return mContext.getString(R.string.screen_tuning);

                case 2:
                    return mContext.getText(R.string.screen_rc);

                case 3:
                    return mContext.getString(R.string.screen_modes);

                case 4:
                    return mContext.getString(R.string.screen_checklist);

                case 5:
                    return mContext.getText(R.string.screen_parameters);

                default:
                    return null;
            }
        }

    }

}
