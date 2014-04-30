package org.droidplanner.android.activities.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.view.Surface;
import android.view.WindowManager;

public class ScreenOrientation {
	public int screenRequestedOrientation;
	private Activity activity;

	public ScreenOrientation(Activity activity) {
		this.activity = activity;
	}

	public void requestLock() {
		if (isPrefLockOrientationSet()) {
			lockOrientation();
		}
	}

	public void unlock() {
		if (screenRequestedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
			screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
			setOrientation();
		}
	}

	private void setOrientation() {
		activity.setRequestedOrientation(screenRequestedOrientation);
	}

	private void lockOrientation() {
		int rotation = ((WindowManager) activity
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
				.getRotation();
		int actualOrientation = activity.getResources().getConfiguration().orientation;
		boolean naturalOrientationLandscape = (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && actualOrientation == Configuration.ORIENTATION_LANDSCAPE) || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && actualOrientation == Configuration.ORIENTATION_PORTRAIT));
		if (naturalOrientationLandscape) {
			switch (rotation) {
			case Surface.ROTATION_0:
				screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_90:
				screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				break;
			case Surface.ROTATION_180:
				screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				break;
			case Surface.ROTATION_270:
				screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			}
		} else {
			switch (rotation) {
			case Surface.ROTATION_0:
				screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
			case Surface.ROTATION_90:
				screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				break;
			case Surface.ROTATION_180:
				screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
				break;
			case Surface.ROTATION_270:
				screenRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
				break;
			}
		}
		setOrientation();
	}

	private boolean isPrefLockOrientationSet() {
		return PreferenceManager.getDefaultSharedPreferences(
				activity.getApplicationContext()).getBoolean(
				"pref_lock_screen_orientation", false);
	}
}
