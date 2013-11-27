package com.droidplanner.activitys;

import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperUI;
import com.droidplanner.widgets.adapterViews.ConfigurationPagerAdapter;

public class ConfigurationActivity extends SuperUI{

    /**
     * Activity title.
     * Used to update the action bar when the navigation drawer opens/closes.
     * @since 1.2.0
     */
    public static final int LABEL_RESOURCE = R.string.configuration;

    /**
     * Activity logo.
     * Used by the navigation drawer.
     * @since 1.2.0
     */
    public static final int LOGO_RESOURCE = R.drawable.ic_action_gear;

    /**
     * View pager allowing to swipe right/left to access the different configuration tabs.
     * @since 1.2.0
     */
    private ViewPager mViewPager;

    /**
     * Adapter for the view pager
     */
    private FragmentPagerAdapter mPagerAdapter;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuration);

        setupNavDrawer();

        mPagerAdapter = new ConfigurationPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.configuration_view_pager);
        mViewPager.setAdapter(mPagerAdapter);
	}

    @Override
    protected int getLabelResource(){
        return LABEL_RESOURCE;
    }


}
