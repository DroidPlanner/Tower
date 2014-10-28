package org.droidplanner.android.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.droidplanner.R;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.dialogs.EditInputDialog;
import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.dialogs.openfile.OpenParameterDialog;
import org.droidplanner.android.dialogs.parameters.DialogParameterInfo;
import org.droidplanner.android.fragments.helpers.ApiListenerListFragment;
import org.droidplanner.android.utils.file.FileStream;
import org.droidplanner.android.utils.file.IO.ParameterWriter;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.widgets.adapterViews.ParamsAdapter;
import org.droidplanner.android.widgets.adapterViews.ParamsAdapterItem;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.profiles.Parameters;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.core.parameters.ParameterMetadata;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ParamsFragment extends ApiListenerListFragment implements OnDroneListener,
		DroneInterfaces.OnParameterManagerListener {

	static final String TAG = ParamsFragment.class.getSimpleName();

	public static final String ADAPTER_ITEMS = ParamsFragment.class.getName() + ".adapter.items";
    private static final String PREF_PARAMS_FILTER_ON = "pref_params_filter_on";
    private static final boolean DEFAULT_PARAMS_FILTER_ON = true;

    private ProgressDialog progressDialog;

    private ProgressBar mLoadingProgress;
    private EditText mParamsFilter;

    private Drone drone;
    private DroidPlannerPrefs mPrefs;
	private ParamsAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mPrefs = new DroidPlannerPrefs(getActivity().getApplicationContext());

		// create adapter
		if (savedInstanceState != null) {
			// load adapter items
			@SuppressWarnings("unchecked")
			final ArrayList<ParamsAdapterItem> pwms = (ArrayList<ParamsAdapterItem>) savedInstanceState
					.getSerializable(ADAPTER_ITEMS);
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
		return inflater.inflate(R.layout.fragment_params, container, false);
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        mLoadingProgress = (ProgressBar) view.findViewById(R.id.reload_progress);
        mLoadingProgress.setVisibility(View.GONE);

        mParamsFilter = (EditText) view.findViewById(R.id.parameter_filter);
        mParamsFilter.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    disableParameterFilter();
                }
            }
        });
        mParamsFilter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_UP:
                        enableParameterFilter();
                        break;
                }
                return false;
            }
        });
        mParamsFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterInput(s.toString());
            }
        });
        mParamsFilter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                filterInput(v.getText());

                if(actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_SEARCH){
                    mParamsFilter.clearFocus();
                }
                return true;
            }
        });


        // listen for clicks on empty
        view.findViewById(android.R.id.empty).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshParameters();
            }
        });

    }

    private void enableParameterFilter() {
        mParamsFilter.setInputType(InputType.TYPE_CLASS_TEXT);
        mParamsFilter.requestFocus();

        final Context context = getActivity();
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context
                .INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(mParamsFilter, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void disableParameterFilter() {
        mParamsFilter.setInputType(InputType.TYPE_NULL);

        final Context context = getActivity();
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context
                .INPUT_METHOD_SERVICE);
        if(imm != null) {
            imm.hideSoftInputFromWindow(mParamsFilter.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    private void filterInput(CharSequence input){
        if(TextUtils.isEmpty(input)){
            adapter.getFilter().filter("");
        }
        else{
            adapter.getFilter().filter(input);
        }
    }

    @Override
    public void onApiConnected(DroidPlannerApi api) {
        DroidPlannerApi dpApi = getApi();
        this.drone = dpApi.getDrone();
        Parameters droneParams = drone.getParameters();

        if(adapter.isEmpty()) {
            List<Parameter> parametersList = droneParams.getParametersList();
            if (!parametersList.isEmpty())
                loadAdapter(parametersList);
        }

        dpApi.addDroneListener(this);
        droneParams.setParameterListener(this);
        toggleParameterFilter(isParameterFilterVisible(), false);
    }

    @Override
    public void onApiDisconnected() {
        DroidPlannerApi dpApi = getApi();
        dpApi.removeDroneListener(this);
        dpApi.getDrone().getParameters().setParameterListener(null);
    }

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
        switch(event){
            case TYPE:
                adapter.loadMetadata(drone);
                break;

            case DISCONNECTED:
                stopProgress();
                break;
        }
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save adapter items
		final ArrayList<ParamsAdapterItem> pwms = new ArrayList<ParamsAdapterItem>(adapter
                .getOriginalValues());
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

        case R.id.menu_filter_params:
            final boolean isEnabled = !isParameterFilterVisible();
            toggleParameterFilter(isEnabled, isEnabled);
            break;

		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

    private void toggleParameterFilter(boolean isVisible, boolean enableInput){
        if(isVisible){
            //Show the parameter filter
            mParamsFilter.setVisibility(View.VISIBLE);
            filterInput(mParamsFilter.getText());

            if(enableInput) {
                enableParameterFilter();
            }
            else{
                disableParameterFilter();
            }
        }
        else{
            //Hide the parameter filter
            disableParameterFilter();
            mParamsFilter.setVisibility(View.GONE);
            filterInput(null);
        }

        mPrefs.prefs.edit().putBoolean(PREF_PARAMS_FILTER_ON, isVisible).apply();
    }

    private boolean isParameterFilterVisible(){
        return mPrefs.prefs.getBoolean(PREF_PARAMS_FILTER_ON,
                DEFAULT_PARAMS_FILTER_ON);
    }

	private void showInfo(int position, EditText valueView) {
		final ParamsAdapterItem item = adapter.getItem(position);
		final ParameterMetadata metadata = item.getMetadata();
		if (metadata == null || !metadata.hasInfo())
			return;

		DialogParameterInfo.build(item, valueView, getActivity()).show();
	}

	private void refreshParameters() {
		if (drone.getMavClient().isConnected()) {
			drone.getParameters().refreshParameters();
		} else {
			Toast.makeText(getActivity(), R.string.msg_connect_first, Toast.LENGTH_SHORT).show();
		}
	}

	private void writeModifiedParametersToDrone() {
		int written = 0;
		for (int i = 0; i < adapter.getCount(); i++) {
			final ParamsAdapterItem item = adapter.getItem(i);
			if (!item.isDirty())
				continue;

			drone.getParameters().sendParameter(item.getParameter());
			item.commit();

			written++;
		}
		if (written > 0)
			adapter.notifyDataSetChanged();
		Toast.makeText(getActivity(),
				written + " " + getString(R.string.msg_parameters_written_to_drone),
				Toast.LENGTH_SHORT).show();
	}

	private void openParametersFromFile() {
		OpenFileDialog dialog = new OpenParameterDialog() {
			@Override
			public void parameterFileLoaded(List<Parameter> parameters) {
				loadAdapter(parameters);
			}
		};
		dialog.openDialog(getActivity());
	}

	private void saveParametersToFile() {
        final Context context = getActivity().getApplicationContext();
        final EditInputDialog dialog = EditInputDialog.newInstance(context,
                getString(R.string.label_enter_filename), FileStream.getParameterFilename("Parameters-"),
                new EditInputDialog.Listener() {
                    @Override
                    public void onOk(CharSequence input) {
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
                    }

                    @Override
                    public void onCancel() {}
                });

        dialog.show(getChildFragmentManager(), "Parameters filename");
	}

	@Override
	public void onBeginReceivingParameters() {
		startProgress();
	}

	@Override
	public void onParameterReceived(Parameter parameter, int index, int count) {
        updateProgress(index, count);
	}

	@Override
	public void onEndReceivingParameters(List<Parameter> parameters) {
        loadAdapter(parameters);
		stopProgress();
	}

    private void loadAdapter(List<Parameter> parameters){
        if(parameters == null || parameters.isEmpty()){
            return;
        }

        Set<Parameter> prunedParameters = new TreeSet<Parameter>(parameters);
        adapter.loadParameters(drone, prunedParameters);

        if(mParamsFilter != null && mParamsFilter.getVisibility() == View.VISIBLE){
            mParamsFilter.setText("");
        }
        else{
            filterInput(null);
        }
    }

    private void startProgress(){
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
