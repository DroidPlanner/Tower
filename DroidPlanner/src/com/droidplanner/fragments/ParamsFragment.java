package com.droidplanner.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.*;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.adapters.ParamsAdapterItem;
import com.droidplanner.dialogs.openfile.OpenFileDialog;
import com.droidplanner.dialogs.openfile.OpenParameterDialog;
import com.droidplanner.drone.Drone;
import com.droidplanner.adapters.ParamsAdapter;
import com.droidplanner.parameters.Parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Date: 2013-12-08
 * Time: 6:27 PM
 */
public class ParamsFragment extends ListFragment {
    public static final String ADAPTER_ITEMS = ParamsFragment.class.getName() + ".adapter.items";

    private Drone drone;
    private ParamsAdapter adapter;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create adapter
        if(savedInstanceState != null) {
            // load adapter items
            final ArrayList<ParamsAdapterItem> pwms =
                    (ArrayList<ParamsAdapterItem>) savedInstanceState.getSerializable(ADAPTER_ITEMS);
            adapter = new ParamsAdapter(getActivity(), R.layout.row_params, pwms);

        } else {
            // empty adapter
            adapter = new ParamsAdapter(getActivity(), R.layout.row_params);
        }
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // bind & initialize UI
        final View view = inflater.inflate(R.layout.fragment_params, container, false);

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save adapter items
        final ArrayList<ParamsAdapterItem> pwms = new ArrayList<ParamsAdapterItem>();
        for(int i = 0; i < adapter.getCount(); i++)
            pwms.add(adapter.getItem(i));
        outState.putSerializable(ADAPTER_ITEMS, pwms);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_parameters, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_open_parameters:
                openParametersFromFile();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void openParametersFromFile() {
        OpenFileDialog dialog = new OpenParameterDialog() {
            @Override
            public void parameterFileLoaded(List<Parameter> parameters) {
                Collections.sort(parameters, new Comparator<Parameter>() {
                    @Override
                    public int compare(Parameter p1, Parameter p2) {
                        return p1.name.compareTo(p2.name);
                    }
                });
                // load parameters from file
                drone.parameters.loadMetadata(getActivity(), null);
                adapter.clear();
                for (Parameter parameter : parameters)
                    adapterAdd(parameter);
            }
        };
        dialog.openDialog(getActivity());
    }

    private void adapterAdd(Parameter parameter) {
        try {
            Parameter.checkParameterName(parameter.name);
            adapter.add(new ParamsAdapterItem(parameter, drone.parameters.getMetadata(parameter.name)));

        } catch (Exception ex) {
            // eat it
        }
    }
}
