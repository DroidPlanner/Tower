package com.droidplanner.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.adapters.ParamsAdapterItem;
import com.droidplanner.dialogs.openfile.OpenFileDialog;
import com.droidplanner.dialogs.openfile.OpenParameterDialog;
import com.droidplanner.dialogs.parameters.DialogParameterInfo;
import com.droidplanner.dialogs.parameters.DialogParameterValues;
import com.droidplanner.drone.Drone;
import com.droidplanner.adapters.ParamsAdapter;
import com.droidplanner.drone.DroneInterfaces;
import com.droidplanner.file.IO.ParameterWriter;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterMetadata;
import com.droidplanner.widgets.adapterViews.ParamRow;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Date: 2013-12-08
 * Time: 6:27 PM
 */
public class ParamsFragment extends ListFragment
        implements DroneInterfaces.OnParameterManagerListner, DroneInterfaces.VehicleTypeListener {
    public static final String ADAPTER_ITEMS = ParamsFragment.class.getName() + ".adapter.items";

    private ProgressDialog pd;

    private Drone drone;
    private ParamsAdapter adapter;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
        drone.parameters.parameterListner = this;
        drone.setVehicleTypeListener(this);
    }

    @Override
    public void onDetach() {
        drone.parameters.parameterListner = null;

        super.onDetach();
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

        // help handler
        adapter.setOnInfoListener(new ParamsAdapter.OnInfoListener() {
            @Override
            public void onHelp(int position, EditText valueView) {
                showInfo(position, valueView);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // bind & initialize UI
        final View view = inflater.inflate(R.layout.fragment_params, container, false);

        // listen for clicks on empty
        view.findViewById(android.R.id.empty).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshParameters();
            }
        });

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
        adapter.clearFocus();

        switch (item.getItemId()) {
            case R.id.menu_load_parameters:
                refreshParameters();
                break;

            case R.id.menu_write_parameters:
                writeModifiedParametersToDrone();
                break;

            case R.id.menu_open_parameters:
                openParametersFromFile();
                break;

            case R.id.menu_save_parameters:
                saveParametersToFile();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void showInfo(int position, EditText valueView) {
        final ParamsAdapterItem item = adapter.getItem(position);
        final ParameterMetadata metadata = item.getMetadata();
        if(metadata == null || !metadata.hasInfo())
            return;

        DialogParameterInfo.build(item, valueView, getActivity()).show();
    }

    private void refreshParameters() {
        if (drone.MavClient.isConnected()) {
            drone.parameters.getAllParameters();
        } else {
            Toast.makeText(getActivity(), "Please connect first", Toast.LENGTH_SHORT).show();
        }
    }

    private void writeModifiedParametersToDrone() {
        int written = 0;
        for(int i = 0; i < adapter.getCount(); i++) {
            final ParamsAdapterItem item = adapter.getItem(i);
            if(!item.isDirty())
                continue;

            drone.parameters.sendParameter(item.getParameter());
            item.commit();

            written++;
        }
        if(written > 0)
            adapter.notifyDataSetChanged();
        Toast.makeText(getActivity(), written + " parameters written to drone", Toast.LENGTH_SHORT).show();
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
                drone.parameters.loadMetadata();
                adapter.clear();
                for (Parameter parameter : parameters)
                    adapterAdd(parameter);
            }
        };
        dialog.openDialog(getActivity());
    }

    private void saveParametersToFile() {
        final List<Parameter> parameters = new ArrayList<Parameter>();
        for(int i = 0; i < adapter.getCount(); i++)
            parameters.add(adapter.getItem(i).getParameter());

        if (parameters.size() > 0) {
            ParameterWriter parameterWriter = new ParameterWriter(parameters);
            if (parameterWriter.saveParametersToFile()) {
                Toast.makeText(getActivity(), getResources().getString(R.string.parameters_saved),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void adapterAdd(Parameter parameter) {
        try {
            Parameter.checkParameterName(parameter.name);
            adapter.add(new ParamsAdapterItem(parameter, drone.parameters.getMetadata(parameter.name)));

        } catch (Exception ex) {
            // eat it
        }
    }

    @Override
    public void onBeginReceivingParameters() {
        pd = new ProgressDialog(getActivity());
        pd.setTitle(getResources().getString(R.string.refreshing_parameters));
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(true);

        pd.show();
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        if (pd != null) {
            if (pd.isIndeterminate()) {
                pd.setIndeterminate(false);
                pd.setMax(count);
            }
            pd.setProgress(index);
        }
    }

    @Override
    public void onEndReceivingParameters(List<Parameter> parameters) {
        Collections.sort(parameters, new Comparator<Parameter>() {
            @Override
            public int compare(Parameter p1, Parameter p2) {
                return p1.name.compareTo(p2.name);
            }
        });
        drone.parameters.loadMetadata();
        adapter.clear();
        for (Parameter parameter : parameters)
            adapterAdd(parameter);

        // dismiss progress dialog
        if (pd != null) {
            pd.dismiss();
            pd = null;
        }
    }

    @Override
    public void onVehicleTypeChanged() {
        drone.parameters.loadMetadata();

        for(int i = 0; i < adapter.getCount(); i++) {
            final ParamsAdapterItem item = adapter.getItem(i);
            item.setMetadata(drone.parameters.getMetadata(item.getParameter().name));
        }
        adapter.notifyDataSetChanged();
    }
}
