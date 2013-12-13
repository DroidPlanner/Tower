package com.droidplanner.activitys;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentTabHost;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperUI;
import com.droidplanner.fragments.*;

public class ConfigurationActivity extends SuperUI{

	public static final String SCREEN_INTENT = "screen";
	public static final String TUNING = "tuning";
	public static final String PARAMETERS = "parameters";
    public static final String SETTINGS = "settings";
	private static final String RC_SETUP = "rc_setup";
	private static final String MODES_SETUP = "flight_modes";
	private static final String CHECKLIST = "flight_checklist";
	private FragmentTabHost mTabHost;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuration);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		mTabHost = (FragmentTabHost)findViewById(R.id.configurationTabHost);
	    mTabHost.setup(this, getFragmentManager(), R.id.realtabcontent);

	    mTabHost.addTab(mTabHost.newTabSpec(SETTINGS).setIndicator("Settings"),SettingsFragment.class, null);
	    mTabHost.addTab(mTabHost.newTabSpec(TUNING).setIndicator("Tuning"),TuningFragment.class, null);
	    mTabHost.addTab(mTabHost.newTabSpec(RC_SETUP).setIndicator("RC"),RcSetupFragment.class, null);
	    mTabHost.addTab(mTabHost.newTabSpec(MODES_SETUP).setIndicator("Modes"),ModesSetupFragment.class, null);
	    mTabHost.addTab(mTabHost.newTabSpec(CHECKLIST).setIndicator("Checklist"),ChecklistFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(PARAMETERS).setIndicator("Parameters"),ParamsFragment.class, null);

	    Intent intent = getIntent();
	    String stringExtra = intent.getStringExtra(SCREEN_INTENT);
		if(SETTINGS.equalsIgnoreCase(stringExtra)){
	    	mTabHost.setCurrentTabByTag(SETTINGS);
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

}
