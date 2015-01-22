package org.droidplanner.android.fragments.helpers;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;

import com.o3dr.android.client.Drone;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.unit.UnitManager;
import org.droidplanner.android.utils.unit.providers.area.AreaUnitProvider;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.utils.unit.providers.speed.SpeedUnitProvider;
import org.droidplanner.android.utils.unit.systems.UnitSystem;

/**
 * Provides access to the DroidPlannerApi to its derived class.
 */
public abstract class ApiListenerDialogFragment extends DialogFragment implements
        DroidPlannerApp.ApiListener {

    private DroidPlannerApp dpApp;
    private LocalBroadcastManager broadcastManager;

    private LengthUnitProvider lengthUnitProvider;
    private AreaUnitProvider areaUnitProvider;
    private SpeedUnitProvider speedUnitProvider;

    protected MissionProxy getMissionProxy(){ return dpApp.getMissionProxy();}
    protected Drone getDrone(){
        return dpApp.getDrone();
    }

    protected LocalBroadcastManager getBroadcastManager(){
        return broadcastManager;
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

    protected Context getContext(){
        return getActivity().getApplicationContext();
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        dpApp = (DroidPlannerApp) activity.getApplication();

        final Context context = activity.getApplicationContext();
        broadcastManager = LocalBroadcastManager.getInstance(activity.getApplicationContext());

        final UnitSystem unitSystem = UnitManager.getUnitSystem(context);
        lengthUnitProvider = unitSystem.getLengthUnitProvider();
        areaUnitProvider = unitSystem.getAreaUnitProvider();
        speedUnitProvider = unitSystem.getSpeedUnitProvider();
    }

    @Override
    public void onStart(){
        super.onStart();

        final UnitSystem unitSystem = UnitManager.getUnitSystem(getContext());
        lengthUnitProvider = unitSystem.getLengthUnitProvider();
        areaUnitProvider = unitSystem.getAreaUnitProvider();
        speedUnitProvider = unitSystem.getSpeedUnitProvider();

        dpApp.addApiListener(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        dpApp.removeApiListener(this);
    }
}
