package co.aerobotics.android.fragments.helpers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.fragments.SettingsFragment;
import co.aerobotics.android.utils.prefs.DroidPlannerPrefs;
import co.aerobotics.android.utils.unit.UnitManager;
import co.aerobotics.android.utils.unit.systems.UnitSystem;
import com.o3dr.android.client.Drone;

import co.aerobotics.android.proxy.mission.MissionProxy;
import co.aerobotics.android.utils.sound.SoundManager;
import co.aerobotics.android.utils.unit.providers.area.AreaUnitProvider;
import co.aerobotics.android.utils.unit.providers.length.LengthUnitProvider;
import co.aerobotics.android.utils.unit.providers.speed.SpeedUnitProvider;

/**
 * Provides access to the DroidPlannerApi to its derived class.
 */
public abstract class ApiListenerFragment extends Fragment implements DroidPlannerApp.ApiListener {

    private static final IntentFilter filter = new IntentFilter();
    static {
        filter.addAction(SettingsFragment.ACTION_PREF_UNIT_SYSTEM_UPDATE);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case SettingsFragment.ACTION_PREF_UNIT_SYSTEM_UPDATE:
                    setupUnitProviders(context);
                    break;
            }
        }
    };

	private DroidPlannerApp dpApp;
	private LocalBroadcastManager broadcastManager;

    private LengthUnitProvider lengthUnitProvider;
    private AreaUnitProvider areaUnitProvider;
    private SpeedUnitProvider speedUnitProvider;

    protected MissionProxy getMissionProxy() { return dpApp.getMissionProxy(); }

    protected DroidPlannerPrefs getAppPrefs(){
        return DroidPlannerPrefs.getInstance(getContext());
    }

	public Drone getDrone() {
		return dpApp.getDrone();
	}

    protected SoundManager getSoundManager(){
        return dpApp.getSoundManager();
    }

	protected LocalBroadcastManager getBroadcastManager() {
		return broadcastManager;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		dpApp = DroidPlannerApp.getInstance();

        final Context context = activity.getApplicationContext();
        broadcastManager = LocalBroadcastManager.getInstance(context);

        setupUnitProviders(context);
	}

	@Override
	public void onStart() {
		super.onStart();

        setupUnitProviders(getContext());
        broadcastManager.registerReceiver(receiver, filter);

        dpApp.addApiListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		dpApp.removeApiListener(this);

        broadcastManager.unregisterReceiver(receiver);
	}

    protected LengthUnitProvider getLengthUnitProvider(){
        return lengthUnitProvider;
    }

    protected AreaUnitProvider getAreaUnitProvider(){
        return areaUnitProvider;
    }

    protected SpeedUnitProvider getSpeedUnitProvider(){
        return speedUnitProvider;
    }

    private void setupUnitProviders(Context context){
        if(context == null)
            return;

        final UnitSystem unitSystem = UnitManager.getUnitSystem(context);
        lengthUnitProvider = unitSystem.getLengthUnitProvider();
        areaUnitProvider = unitSystem.getAreaUnitProvider();
        speedUnitProvider = unitSystem.getSpeedUnitProvider();
    }
}
