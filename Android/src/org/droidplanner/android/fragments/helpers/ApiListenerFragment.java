package org.droidplanner.android.fragments.helpers;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import com.o3dr.android.client.Drone;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.unit.UnitManager;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.utils.unit.providers.speed.SpeedUnitProvider;
import org.droidplanner.android.utils.unit.systems.UnitSystem;

/**
 * Provides access to the DroidPlannerApi to its derived class.
 */
public abstract class ApiListenerFragment extends Fragment implements DroidPlannerApp.ApiListener {

	private DroidPlannerApp dpApp;
	private LocalBroadcastManager broadcastManager;

    protected MissionProxy getMissionProxy() { return dpApp.getMissionProxy(); }

    protected DroidPlannerPrefs getAppPrefs(){
        return dpApp.getAppPreferences();
    }

	protected Drone getDrone() {
		return dpApp.getDrone();
	}

	protected LocalBroadcastManager getBroadcastManager() {
		return broadcastManager;
	}

    protected LengthUnitProvider getLengthUnitProvider(){
        final Context context = getContext();
        if(context == null)
            return null;

        final UnitSystem unitSystem = UnitManager.getUnitSystem(context);
        return unitSystem.getLengthUnitProvider();
    }

    protected SpeedUnitProvider getSpeedUnitProvider() {
        final Context context = getContext();
        if(context == null)
            return null;

        final UnitSystem unitSystem = UnitManager.getUnitSystem(context);
        return unitSystem.getSpeedUnitProvider();
    }

    protected Context getContext(){
        return getActivity().getApplicationContext();
    }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		dpApp = (DroidPlannerApp) activity.getApplication();

        final Context context = activity.getApplicationContext();
		broadcastManager = LocalBroadcastManager.getInstance(context);
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
