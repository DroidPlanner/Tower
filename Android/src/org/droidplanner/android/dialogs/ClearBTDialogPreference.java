package org.droidplanner.android.dialogs;

import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class ClearBTDialogPreference extends DialogPreference {

	private DroidPlannerPrefs mAppPrefs;

	public ClearBTDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAppPrefs = new DroidPlannerPrefs(context.getApplicationContext());
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			mAppPrefs.setBluetoothDeviceAddress("");
		}
	}

}
