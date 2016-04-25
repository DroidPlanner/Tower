package org.droidplanner.android.dialogs;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

public class ClearBTDialogPreference extends DialogPreference {

    public interface OnResultListener {
        void onResult(boolean result);
    }

	private DroidPlannerPrefs mAppPrefs;

    private OnResultListener listener;

	public ClearBTDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAppPrefs = DroidPlannerPrefs.getInstance(context);
	}

	protected void onDialogClosed(boolean positiveResult) {
		//TODO: figure out replacement
//		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			mAppPrefs.setBluetoothDeviceAddress("");
		}

        if(listener != null)
            listener.onResult(positiveResult);
	}

    public void setOnResultListener(OnResultListener listener){
        this.listener = listener;
    }

}
