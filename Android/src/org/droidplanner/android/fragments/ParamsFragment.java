package org.droidplanner.android.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.Parameters;

import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.SupportEditInputDialog;
import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.dialogs.openfile.OpenParameterDialog;
import org.droidplanner.android.dialogs.parameters.DialogParameterInfo;
import org.droidplanner.android.fragments.helpers.ApiListenerListFragment;
import org.droidplanner.android.utils.file.FileStream;
import org.droidplanner.android.utils.file.IO.ParameterWriter;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.view.adapterViews.ParamsAdapter;
import org.droidplanner.android.view.adapterViews.ParamsAdapterItem;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ParamsFragment extends ApiListenerListFragment implements SupportEditInputDialog.Listener {

    public static final String ADAPTER_ITEMS = ParamsFragment.class.getName() + ".adapter.items";
    private static final String PREF_PARAMS_FILTER_ON = "pref_params_filter_on";
    private static final boolean DEFAULT_PARAMS_FILTER_ON = true;

    private static final String EXTRA_OPENED_PARAMS_FILENAME = "extra_opened_params_filename";

    private final static IntentFilter intentFilter = new IntentFilter();
    public static final int SNACKBAR_HEIGHT = 48;
    private static final String PARAMETERS_FILENAME_DIALOG_TAG = "Parameters filename";

    static {
        intentFilter.addAction(AttributeEvent.PARAMETERS_REFRESH_STARTED);
        intentFilter.addAction(AttributeEvent.PARAMETERS_REFRESH_COMPLETED);
        intentFilter.addAction(AttributeEvent.PARAMETER_RECEIVED);
        intentFilter.addAction(AttributeEvent.STATE_CONNECTED);
        intentFilter.addAction(AttributeEvent.TYPE_UPDATED);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.PARAMETERS_REFRESH_STARTED:
                    startProgress();
                    break;

                case AttributeEvent.PARAMETERS_REFRESH_COMPLETED:
                    stopProgress();
                    /*** FALL - THROUGH ***/
                case AttributeEvent.STATE_CONNECTED:
                case AttributeEvent.TYPE_UPDATED:
                    final Drone drone = getDrone();
                    if (drone != null && drone.isConnected()) {
                        final Parameters droneParams = drone.getAttribute(AttributeType.PARAMETERS);
                        loadAdapter(droneParams.getParameters(), false);
                    }
                    break;

                case AttributeEvent.PARAMETER_RECEIVED:
                    final int defaultValue = -1;
                    int index = intent.getIntExtra(AttributeEventExtra.EXTRA_PARAMETER_INDEX, defaultValue);
                    int count = intent.getIntExtra(AttributeEventExtra.EXTRA_PARAMETERS_COUNT, defaultValue);

                    if (index != defaultValue && count != defaultValue)
                        updateProgress(index, count);
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                    stopProgress();
                    break;
            }
        }
    };

    private ProgressDialog progressDialog;
    private SearchView searchParams;
    private ProgressBar mLoadingProgress;

    private DroidPlannerPrefs mPrefs;
    private ParamsAdapter adapter;

    /**
     * If the parameters were loaded from a file, the filename is stored here.
     */
    private String openedParamsFilename;
    private View searchButton;
    private Snackbar snackbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mPrefs = new DroidPlannerPrefs(getActivity().getApplicationContext());

        // create adapter
        if (savedInstanceState != null) {
            this.openedParamsFilename = savedInstanceState.getString(EXTRA_OPENED_PARAMS_FILENAME);

            // load adapter items
            @SuppressWarnings("unchecked")
            final ArrayList<ParamsAdapterItem> pwms = savedInstanceState.getParcelableArrayList(ADAPTER_ITEMS);
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
        adapter.setOnParametersChangeListener(new ParamsAdapter.OnParametersChangeListener() {
            @Override
            public void onParametersChange(int dirtyCount) {
                if (dirtyCount > 0) {
                    View view = getView();
                    if (view != null && snackbar == null) {
                        snackbar = Snackbar.make(view, R.string.unsaved_param_warning, Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.upload), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        writeModifiedParametersToDrone();
                                    }
                                });
                        snackbar.show();
                    }

                } else {
                    if (snackbar != null) {
                        snackbar.dismiss();
                        snackbar = null;
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // bind & initialize UI
        return inflater.inflate(R.layout.fragment_params, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchParams = (SearchView) view.findViewById(R.id.params_filter);
        searchParams.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                filterInput(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterInput(s);
                return true;
            }
        });
        searchButton = searchParams.findViewById(android.support.v7.appcompat.R.id.search_button);
        searchParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                    searchButton.performClick();
                else
                    searchButton.callOnClick();
            }
        });

        mLoadingProgress = (ProgressBar) view.findViewById(R.id.reload_progress);
        mLoadingProgress.setVisibility(View.GONE);

        View space = new View(getActivity().getApplicationContext());
        space.setLayoutParams(new AbsListView.LayoutParams(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SNACKBAR_HEIGHT, getResources().getDisplayMetrics())));
        getListView().addFooterView(space);
    }

    private void filterInput(CharSequence input) {
        if (TextUtils.isEmpty(input)) {
            adapter.getFilter().filter("");
        } else {
            adapter.getFilter().filter(input);
        }
    }

    @Override
    public void onApiConnected() {
        final Parameters droneParams = getDrone().getAttribute(AttributeType.PARAMETERS);

        if (adapter.isEmpty() && droneParams != null) {
            List<Parameter> parametersList = droneParams.getParameters();
            if (!parametersList.isEmpty())
                loadAdapter(parametersList, false);
        }

        toggleParameterFilter(isParameterFilterVisible());

        getBroadcastManager().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save adapter items
        final ArrayList<ParamsAdapterItem> pwms = new ArrayList<ParamsAdapterItem>(adapter.getOriginalValues());
        outState.putParcelableArrayList(ADAPTER_ITEMS, pwms);

        outState.putString(EXTRA_OPENED_PARAMS_FILENAME, this.openedParamsFilename);
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
            case R.id.menu_download_parameters:
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

    private void toggleParameterFilter(boolean isVisible) {
        if (isVisible) {
            //Show the parameter filter
            searchParams.setVisibility(View.VISIBLE);
            filterInput(searchParams.getQuery());

        } else {
            //Hide the parameter filter
            searchParams.setVisibility(View.GONE);
            filterInput(null);
        }

        mPrefs.prefs.edit().putBoolean(PREF_PARAMS_FILTER_ON, isVisible).apply();
    }

    private boolean isParameterFilterVisible() {
        return mPrefs.prefs.getBoolean(PREF_PARAMS_FILTER_ON, DEFAULT_PARAMS_FILTER_ON);
    }

    private void showInfo(int position, EditText valueView) {
        final ParamsAdapterItem item = adapter.getItem(position);
        if (!item.getParameter().hasInfo())
            return;

        DialogParameterInfo.build(item, valueView, getActivity()).show();
    }

    private void refreshParameters() {
        if (getDrone().isConnected()) {
            VehicleApi.getApi(getDrone()).refreshParameters();
        } else {
            Toast.makeText(getActivity(), R.string.msg_connect_first, Toast.LENGTH_SHORT).show();
        }
    }

    private void writeModifiedParametersToDrone() {
        final Drone drone = getDrone();
        if (!drone.isConnected())
            return;

        final int adapterCount = adapter.getCount();
        List<Parameter> parametersList = new ArrayList<Parameter>(adapterCount);
        for (int i = 0; i < adapterCount; i++) {
            final ParamsAdapterItem item = adapter.getItem(i);
            if (!item.isDirty())
                continue;

            parametersList.add(item.getParameter());
            item.commit();
        }

        final int parametersCount = parametersList.size();
        if (parametersCount > 0) {
            drone.writeParameters(new Parameters(parametersList));
            adapter.notifyDataSetChanged();
            Toast.makeText(getActivity(),
                    parametersCount + " " + getString(R.string.msg_parameters_written_to_drone),
                    Toast.LENGTH_SHORT).show();
        }
        snackbar = null;
    }

    private void openParametersFromFile() {
        OpenFileDialog dialog = new OpenParameterDialog() {
            @Override
            public void parameterFileLoaded(List<Parameter> parameters) {
                openedParamsFilename = getSelectedFilename();
                loadAdapter(parameters, true);
            }
        };
        dialog.openDialog(getActivity());
    }

    private void saveParametersToFile() {
        final String defaultFilename = TextUtils.isEmpty(openedParamsFilename)
                ? FileStream.getParameterFilename("Parameters-")
                : openedParamsFilename;

        final SupportEditInputDialog dialog = SupportEditInputDialog.newInstance(PARAMETERS_FILENAME_DIALOG_TAG,
                getString(R.string.label_enter_filename), defaultFilename, true);

        dialog.show(getChildFragmentManager(), PARAMETERS_FILENAME_DIALOG_TAG);
    }

    @Override
    public void onOk(String dialogTag, CharSequence input) {
        switch(dialogTag){
            case PARAMETERS_FILENAME_DIALOG_TAG:
                final List<Parameter> parameters = new ArrayList<Parameter>();
                for (int i = 0; i < adapter.getCount(); i++) {
                    parameters.add(adapter.getItem(i).getParameter());
                }

                if (parameters.size() > 0) {
                    ParameterWriter parameterWriter = new ParameterWriter(parameters);
                    if (parameterWriter.saveParametersToFile(input.toString())) {
                        Toast.makeText(getActivity(), R.string.parameters_saved, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onCancel(String dialogTag) {
    }

    private void loadAdapter(List<Parameter> parameters, boolean isUpdate) {
        if (parameters == null || parameters.isEmpty()) {
            return;
        }

        TreeMap<String, Parameter> prunedParameters = new TreeMap<String, Parameter>();
        for (Parameter parameter : parameters) {
            prunedParameters.put(parameter.getName(), parameter);
        }

        if (isUpdate) {
            adapter.updateParameters(prunedParameters);
        } else {
            adapter.loadParameters(prunedParameters);
        }

        filterInput(searchParams.getQuery());
    }

    private void startProgress() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(R.string.refreshing_parameters);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();

        mLoadingProgress.setIndeterminate(true);
        mLoadingProgress.setVisibility(View.VISIBLE);
    }

    private void updateProgress(int progress, int max) {
        if (progressDialog == null) {
            startProgress();
        }

        if (progressDialog.isIndeterminate()) {
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(max);
        }
        progressDialog.setProgress(progress);

        if (mLoadingProgress.isIndeterminate()) {
            mLoadingProgress.setIndeterminate(false);
            mLoadingProgress.setMax(max);
        }
        mLoadingProgress.setProgress(progress);
    }

    private void stopProgress() {
        // dismiss progress dialog
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        mLoadingProgress.setVisibility(View.GONE);
    }
}
