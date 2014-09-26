package org.droidplanner.android.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.dialogs.openfile.OpenParameterDialog;
import org.droidplanner.android.dialogs.parameters.DialogParameterInfo;
import org.droidplanner.android.utils.file.IO.ParameterWriter;
import org.droidplanner.android.widgets.adapterViews.ParamsAdapter;
import org.droidplanner.android.widgets.adapterViews.ParamsAdapterItem;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.core.parameters.ParameterMetadata;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

public class ParamsFragment extends ListFragment implements
		DroneInterfaces.OnParameterManagerListener, OnDroneListener, SearchView.OnCloseListener,
        SearchView.OnQueryTextListener {

	static final String TAG = ParamsFragment.class.getSimpleName();

	public static final String ADAPTER_ITEMS = ParamsFragment.class.getName() + ".adapter.items";

	private ProgressDialog progressDialog;

    private ProgressBar mLoadingProgress;
    private SearchView mParamsFilter;

	private Drone drone;
	private ParamsAdapter adapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();

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

            final List<Parameter> parametersList = drone.getParameters().getParametersList();
            if(!parametersList.isEmpty()) {
                adapter.loadParameters(drone, parametersList);
            }
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

        mParamsFilter = (SearchView) view.findViewById(R.id.parameter_filter);
        mParamsFilter.setOnQueryTextListener(this);
        mParamsFilter.setIconifiedByDefault(false);
        mParamsFilter.setSubmitButtonEnabled(false);
        mParamsFilter.setOnCloseListener(this);

        // listen for clicks on empty
        view.findViewById(android.R.id.empty).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshParameters();
            }
        });

    }

	@Override
	public void onStart() {
		super.onStart();
		drone.addDroneListener(this);
		drone.getParameters().setParameterListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		drone.removeDroneListener(this);
		drone.getParameters().setParameterListener(null);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		if (event == DroneEventsType.TYPE) {
			adapter.loadMetadata(drone);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save adapter items
		final ArrayList<ParamsAdapterItem> pwms = new ArrayList<ParamsAdapterItem>();
		for (int i = 0; i < adapter.getCount(); i++)
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
				Collections.sort(parameters, new Comparator<Parameter>() {
					@Override
					public int compare(Parameter p1, Parameter p2) {
						return p1.name.compareTo(p2.name);
					}
				});
				// load parameters from file
				adapter.loadParameters(drone, parameters);
			}
		};
		dialog.openDialog(getActivity());
	}

	private void saveParametersToFile() {
		final List<Parameter> parameters = new ArrayList<Parameter>();
		for (int i = 0; i < adapter.getCount(); i++)
			parameters.add(adapter.getItem(i).getParameter());

		if (parameters.size() > 0) {
			ParameterWriter parameterWriter = new ParameterWriter(parameters);
			if (parameterWriter.saveParametersToFile()) {
				Toast.makeText(getActivity(), R.string.parameters_saved, Toast.LENGTH_SHORT).show();
			}
		}
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
		Collections.sort(parameters, new Comparator<Parameter>() {
			@Override
			public int compare(Parameter p1, Parameter p2) {
				return p1.name.compareTo(p2.name);
			}
		});
		adapter.loadParameters(drone, parameters);

		stopProgress();
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

    @Override
    public boolean onClose() {
        adapter.getFilter().filter("");
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(TextUtils.isEmpty(newText)){
            adapter.getFilter().filter("");
        }
        else{
            adapter.getFilter().filter(newText);
        }
        return true;
    }
}
