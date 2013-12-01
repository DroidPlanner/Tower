package com.droidplanner.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperUI;
import com.droidplanner.widgets.adapterViews.ConfigurationPagerAdapter;
import com.droidplanner.widgets.adapterViews.NavigationHubAdapter;

import static com.droidplanner.utils.Constants.*;

public class ConfigurationActivity extends SuperUI{

    /**
     * Activity title.
     * Used to update the action bar when the navigation drawer opens/closes.
     * @since 1.2.0
     */
    public static final int LABEL_RESOURCE = R.string.screen_configuration;

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
     * Type of navigation hub this activity is.
     * Based on the intent action, and alternates between:
     * <li>
     *     <ul>{@link NavigationHubAdapter.HubItem.CONFIGURATION}</ul>
     *     <ul>{@link NavigationHubAdapter.HubItem.TUNING}</ul>
     *     <ul>{@link NavigationHubAdapter.HubItem.RC}</ul>
     *     <ul>{@link NavigationHubAdapter.HubItem.PARAMETERS}</ul>
     *     <ul>{@link NavigationHubAdapter.HubItem.SETTINGS}</ul>
     * </li>
     */
    private NavigationHubAdapter.HubItem mNavigationHubItem = NavigationHubAdapter.HubItem
            .CONFIGURATION;

    /**
     * Adapter for the view pager
     */
    private FragmentPagerAdapter mPagerAdapter;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuration);

        mPagerAdapter = new ConfigurationPagerAdapter(getApplicationContext(),
                getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.activity_content_view);
        mViewPager.setAdapter(mPagerAdapter);

        handleIntent(getIntent());
	}

    @Override
    public void onNewIntent(Intent newIntent){
        super.onNewIntent(newIntent);
        handleIntent(newIntent);
    }

    @Override
    protected int getLabelResource(){
        return LABEL_RESOURCE;
    }

    @Override
    protected NavigationHubAdapter.HubItem getNavigationHubItem(){
        return mNavigationHubItem;
    }

    private void handleIntent(Intent intent){
        final String action = intent.getAction();

        if(ACTION_CONFIGURATION_TUNING.equals(action)){
            mNavigationHubItem = NavigationHubAdapter.HubItem.TUNING;
            mViewPager.setCurrentItem(0);
        }
        else if(ACTION_CONFIGURATION_RC.equals(action)){
            mNavigationHubItem = NavigationHubAdapter.HubItem.RC;
            mViewPager.setCurrentItem(1);
        }
        else if(ACTION_CONFIGURATION_PARAMETERS.equals(action)){
            mNavigationHubItem = NavigationHubAdapter.HubItem.PARAMETERS;
            mViewPager.setCurrentItem(2);
        }
        else if(ACTION_CONFIGURATION_SETTINGS.equals(action)){
            mNavigationHubItem = NavigationHubAdapter.HubItem.SETTINGS;
            mViewPager.setCurrentItem(3);
        }
        else{
            mNavigationHubItem = NavigationHubAdapter.HubItem.CONFIGURATION;
        }

        setupNavDrawer();
    }


}
