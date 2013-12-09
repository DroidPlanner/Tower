package com.droidplanner.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.TableLayout;
import android.widget.TextView;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.dialogs.openfile.OpenFileDialog;
import com.droidplanner.dialogs.openfile.OpenParameterDialog;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces;
import com.droidplanner.drone.variables.Parameters;
import com.droidplanner.fragments.mode.ParamsAdapter;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.widgets.adapterViews.ParamRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: rgayle
 * Date: 2013-12-08
 * Time: 6:27 PM
 */
public class ParamsFragment extends ListFragment {
    public static final String ADAPTER = ParamsFragment.class.getName() + ".adapter";

    private List<ParamRow> rowList = new ArrayList<ParamRow>();
    private Drone drone;

    private ParamsAdapter adapter;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_params, container, false);

//        parameterTable = (TableLayout) view.findViewById(R.id.parametersTable);
//        for(ParamRow row: rowList)
//            parameterTable.addView(row);
//
//        refreshTextView = (TextView) view.findViewById(R.id.refreshTextView);
//        refreshTextView.setOnClickListener(this);
//        if(!rowList.isEmpty())
//            refreshTextView.setVisibility(View.GONE);

        adapter = new ParamsAdapter(getActivity(), R.layout.row_params);
        setListAdapter(adapter);

        setHasOptionsMenu(true);
        return view;
    }

    //    @Override
//    public void onDestroyView() {
//        for(ParamRow row: rowList)
//            parameterTable.removeView(row);
//
//        super.onDestroyView();
//    }

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
                    adapter.add(new ParamsAdapter.ParameterWithMetadata(parameter, drone.parameters.getMetadata(parameter.name)));
            }
        };
        dialog.openDialog(getActivity());
    }

    public void refreshRowParameter(Parameter parameter, Parameters parameters) {
        try {
            Parameter.checkParameterName(parameter.name);
            ParamRow row = findRowByName(parameter.name);
            if (row != null) {
                row.setParam(parameter, parameters);
            } else {
                addParameterRow(parameter, parameters);
            }
        } catch (Exception e) {
        }
    }

    private ParamRow findRowByName(String name) {
        for (ParamRow row : rowList) {
            if (row.getParamName().equals(name)) {
                return row;
            }
        }
        return null;
    }

    private void addParameterRow(Parameter param, Parameters parameters) {
        ParamRow pRow = new ParamRow(getActivity());
        pRow.setParam(param, parameters);

        // alternate background colors for clarity
        if ((rowList.size() % 2) == 1)
            pRow.setBackgroundColor(Color.rgb(0xF0, 0xF0, 0xF0));

        rowList.add(pRow);
//        parameterTable.addView(pRow);
    }
}
