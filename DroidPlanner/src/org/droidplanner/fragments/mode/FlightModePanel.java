package org.droidplanner.fragments.mode;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.internal.cl;
import org.droidplanner.R;
import org.droidplanner.activitys.helpers.SuperActivity;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces;
import org.droidplanner.widgets.spinners.ModeAdapter;

import java.util.List;

/**
 * @author Fredia Huya-Kouadio
 */
public class FlightModePanel extends Fragment implements DroneInterfaces.OnDroneListner {

    /**
     * This is the parent activity for this fragment.
     */
    private SuperActivity mParentActivity;

    /**
     * This spinner is used to switch between the different flight/apm modes.
     */
    private Spinner mModeSpinner;

    /**
     * Spinner adapter for the flight/apm modes.
     */
    private ModeAdapter mModeAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof SuperActivity)) {
            throw new IllegalStateException("Parent activity must be an instance of " +
                    SuperActivity.class.getName());
        }

        mParentActivity = (SuperActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mParentActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_flight_mode_panel, container, false);

        mModeSpinner = (Spinner) view.findViewById(R.id.flight_mode_spinner);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        onDroneTypeUpdate(mParentActivity.drone.type.getType());

        mModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mParentActivity != null) {
                    ApmModes newMode = (ApmModes) parent.getItemAtPosition(position);
                    mParentActivity.drone.state.changeFlightMode(newMode);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Update the spinner, and the mode info panel based on the current mode.
        onModeUpdate(mParentActivity.drone.state.getMode());

    }

    @Override
    public void onStart(){
        super.onStart();

        if(mParentActivity != null){
            mParentActivity.drone.events.addDroneListener(this);
        }
    }

    @Override
    public void onStop(){
        super.onStop();

        if(mParentActivity != null){
            mParentActivity.drone.events.removeDroneListener(this);
        }
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch (event) {
            case MODE:
                //Update the spinner, and the mode info panel
                onModeUpdate(drone.state.getMode());
                break;

            case TYPE:
                //Update the spinner adapter
                onDroneTypeUpdate(drone.type.getType());
                break;
        }
    }

    private void onDroneTypeUpdate(int type){
        if(mModeSpinner == null)
            return;

        final List<ApmModes> flightModes = ApmModes.getModeList(type);
        if(mModeAdapter == null){
            mModeAdapter = new ModeAdapter(getActivity().getApplicationContext(),
                    R.layout.spinner_flight_mode_item, flightModes);
            mModeSpinner.setAdapter(mModeAdapter);
        }
        else{
            mModeAdapter.clear();
            mModeAdapter.addAll(flightModes);
            mModeAdapter.notifyDataSetChanged();
        }
    }

    private void onModeUpdate(ApmModes mode) {
        if (mModeAdapter != null) {
            //Update the spinner position
            mModeSpinner.setSelection(mModeAdapter.getPosition(mode));

            //Update the info panel fragment
            Fragment infoPanel;
            if (mParentActivity == null || !mParentActivity.drone.MavClient.isConnected()) {
                infoPanel = new ModeDisconnectedFragment();
            }
            else {
                switch (mode) {
                    case ROTOR_RTL:
                        infoPanel = new ModeRTLFragment();
                        break;
                    case ROTOR_AUTO:
                        infoPanel = new ModeAutoFragment();
                        break;
                    case ROTOR_LAND:
                        infoPanel = new ModeLandFragment();
                        break;
                    case ROTOR_LOITER:
                        infoPanel = new ModeLoiterFragment();
                        break;
                    case ROTOR_STABILIZE:
                        infoPanel = new ModeStabilizeFragment();
                        break;
                    case ROTOR_ACRO:
                        infoPanel = new ModeAcroFragment();
                        break;
                    case ROTOR_ALT_HOLD:
                        infoPanel = new ModeAltholdFragment();
                        break;
                    case ROTOR_CIRCLE:
                        infoPanel = new ModeCircleFragment();
                        break;
                    case ROTOR_GUIDED:
                        infoPanel = new ModeGuidedFragment();
                        break;
                    case ROTOR_POSITION:
                        infoPanel = new ModePositionFragment();
                        break;
                    case ROTOR_TOY:
                        infoPanel = new ModeDriftFragment();
                        break;
                    default:
                        infoPanel = new ModeDisconnectedFragment();
                        break;
                }
            }

            getChildFragmentManager().beginTransaction().replace(R.id.modeInfoPanel,
                    infoPanel).commit();
        }
    }
}