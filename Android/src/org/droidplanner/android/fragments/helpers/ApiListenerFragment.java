package org.droidplanner.android.fragments.helpers;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import com.ox3dr.services.android.lib.model.IDroidPlannerApi;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.api.DroneApi;

/**
 * Provides access to the DroidPlannerApi to its derived class.
 */
public abstract class ApiListenerFragment extends Fragment implements DroidPlannerApp.ApiListener {

	private DroidPlannerApp dpApp;
	private LocalBroadcastManager broadcastManager;

	protected DroneApi getDroneApi() {
		return dpApp.getDroneApi();
	}

	protected LocalBroadcastManager getBroadcastManager() {
		return broadcastManager;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		dpApp = (DroidPlannerApp) activity.getApplication();
		broadcastManager = LocalBroadcastManager.getInstance(activity.getApplicationContext());
	}

	@Override
	public void onStart() {
		super.onStart();
		dpApp.addApiListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		dpApp.removeApiListener(this);
	}
}
