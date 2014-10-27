package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.YesNoWithPrefsDialog;
import org.droidplanner.android.fragments.helpers.ApiSubscriberFragment;
import org.droidplanner.android.helpers.ApiInterface;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.variables.Type;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.model.Drone;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.analytics.HitBuilders;

public class FlightActionsFragment extends ApiSubscriberFragment implements OnDroneListener {

    interface SlidingUpHeader{
        boolean isSlidingUpPanelEnabled(DroidPlannerApi api);
    }

    private SlidingUpHeader header;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_flight_actions_bar, container, false);
    }

    @Override
    protected void onApiConnectedImpl(DroidPlannerApi api) {
        selectActionsBar(api.getDrone().getType());
        api.addDroneListener(this);
    }

    @Override
    protected void onApiDisconnectedImpl() {
        getApi().removeDroneListener(this);
    }

    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        switch(event){
            case TYPE:
                final int droneType = drone.getType();
                selectActionsBar(droneType);
                break;
        }
    }

    private void selectActionsBar(int droneType) {
        final FragmentManager fm = getChildFragmentManager();

        Fragment actionsBarFragment;
        if(Type.isCopter(droneType)){
            actionsBarFragment = new CopterFlightActionsFragment();
        }
        else if(Type.isPlane(droneType)){
            actionsBarFragment = new PlaneFlightActionsFragment();
        }
        else{
            actionsBarFragment = new GenericActionsFragment();
        }

        fm.beginTransaction().replace(R.id.flight_actions_bar, actionsBarFragment).commit();
        header = (SlidingUpHeader) actionsBarFragment;
    }

    public boolean isSlidingUpPanelEnabled(DroidPlannerApi api){
        return header != null && header.isSlidingUpPanelEnabled(api);
    }
}
