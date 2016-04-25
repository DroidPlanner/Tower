package org.droidplanner.android.fragments.control;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.o3dr.android.client.Drone;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

/**
 * Provides action buttons functionality for generic drone type.
 */
public class GenericActionsFragment extends BaseFlightControlFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_generic_mission_control, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        final Context context = getActivity().getApplicationContext();
        final DroidPlannerPrefs prefs = DroidPlannerPrefs.getInstance(context);

        final Resources res = getResources();
        final String[] connectionTypes = res.getStringArray(R.array.TelemetryConnectionTypes);
        final String[] connectionTypeValues = res.getStringArray(R.array.TelemetryConnectionTypesValues);

        final SparseArray<String> valuesToLabels = new SparseArray<>();
        for(int i = 0; i < connectionTypes.length; i++){
            valuesToLabels.append(Integer.parseInt(connectionTypeValues[i]), connectionTypes[i]);
        }

        final int savedConnectionType = prefs.getConnectionParameterType();

        View connectBtn = view.findViewById(R.id.mc_connectBtn);
        connectBtn.setOnClickListener(this);

        final ArrayAdapter<String> connectionsAdapter = new ArrayAdapter<>(context, R.layout.spinner_drop_down_mission_item, connectionTypes);

        Spinner connectionsSpinner = (Spinner) view.findViewById(R.id.telem_connection_type);
        connectionsSpinner.setAdapter(connectionsAdapter);
        connectionsSpinner.setSelection(connectionsAdapter.getPosition(valuesToLabels.get(savedConnectionType)));
        connectionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String connectionValue = connectionTypeValues[position];

                //Save the selected connection to the shared preferences
                prefs.setConnectionParameterType(Integer.parseInt(connectionValue));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.mc_connectBtn:
                ((SuperUI) getActivity()).toggleDroneConnection();
                break;
        }
    }

    @Override
    public boolean isSlidingUpPanelEnabled(Drone drone) {
        return true;
    }
}
