package com.droidplanner.activitys;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v13.app.FragmentTabHost;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperUI;
import com.droidplanner.fragments.ParametersTableFragment;
import com.droidplanner.fragments.helpers.OfflineMapFragment;

public class ConfigurationActivity extends SuperUI{

    private FragmentTabHost mTabHost;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuration);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		mTabHost = (FragmentTabHost)findViewById(R.id.configurationTabHost);
	    mTabHost.setup(this, getFragmentManager(), R.id.realtabcontent);
	    mTabHost.addTab(mTabHost.newTabSpec("parameters").setIndicator("Parameters"),
	            ParametersTableFragment.class, null);
	    mTabHost.addTab(mTabHost.newTabSpec("map").setIndicator("MAP"),	//TODO remove this after testing
	            OfflineMapFragment.class, null);
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

}
