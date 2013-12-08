package com.droidplanner.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.Messages.enums.MAV_TYPE;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.dialogs.openfile.OpenFileDialog;
import com.droidplanner.dialogs.openfile.OpenParameterDialog;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.OnParameterManagerListner;
import com.droidplanner.drone.variables.Parameters;
import com.droidplanner.file.IO.ParameterWriter;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.widgets.adapterViews.ParamRow;

public class ParametersTableFragment extends Fragment implements
		OnClickListener, OnParameterManagerListner {

	private TableLayout parameterTable;
	private List<ParamRow> rowList = new ArrayList<ParamRow>();
	private Drone drone;
	private Context context;

	private ProgressDialog pd;
	private TextView refreshTextView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_parameters, container,
				false);
		parameterTable = (TableLayout) view.findViewById(R.id.parametersTable);

		refreshTextView = (TextView) view
				.findViewById(R.id.refreshTextView);
		refreshTextView.setOnClickListener(this);

        setHasOptionsMenu(true);
		return view;
	}



	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        context = activity;

        drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
        drone.parameters.parameterListner = this;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_parameters, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
		ParamRow pRow = new ParamRow(this.getActivity());
		pRow.setParam(param, parameters);

		// alternate background colors for clarity
		if ((rowList.size() % 2) == 1)
			pRow.setBackgroundColor(Color.BLACK);

		rowList.add(pRow);
		parameterTable.addView(pRow);
	}

	private List<Parameter> getParameterListFromTable() {
		ArrayList<Parameter> parameters = new ArrayList<Parameter>();
		for (ParamRow row : rowList) {
			parameters.add(row.getParameterFromRow());
		}
		return parameters;
	}

	public List<ParamRow> getModifiedParametersRows() {
		ArrayList<ParamRow> modParameters = new ArrayList<ParamRow>();
		for (ParamRow row : rowList) {
			if (!row.isNewValueEqualToDroneParam()) {
				modParameters.add(row);
			}
		}
		return modParameters;
	}

	public void refresh(Parameters parameters) {
		for (ParamRow row : rowList)
			row.setParamMetadata(parameters);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.refreshTextView) {
			refreshParameters();
		}

	}

	private void refreshParameters() {
		if (drone.MavClient.isConnected()) {
			drone.parameters.getAllParameters();
		} else {
			Toast.makeText(context, "Please connect first", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void writeModifiedParametersToDrone() {
		List<ParamRow> modRows = getModifiedParametersRows();
		for (ParamRow row : modRows) {
			if (!row.isNewValueEqualToDroneParam()) {
				drone.parameters.sendParameter(row.getParameterFromRow());
			}
		}
		Toast.makeText(context, "Write " + modRows.size() + " parameters",
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
				drone.parameters.loadMetadata(context, null);
				for (Parameter parameter : parameters)
					refreshRowParameter(parameter, drone.parameters);
			}
		};
		dialog.openDialog(context);
	}

    private void saveParametersToFile() {
        List<Parameter> parameterList = getParameterListFromTable();
        if (parameterList.size() > 0) {
            ParameterWriter parameterWriter = new ParameterWriter(parameterList);
            if (parameterWriter.saveParametersToFile()) {
                Toast.makeText(this.getActivity(), "Parameters saved",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

	@Override
	public void onBeginReceivingParameters() {
		pd = new ProgressDialog(context);
		pd.setTitle("Refreshing Parameters...");
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
		drone.parameters.loadMetadata(context, getMetadataType());
		for (Parameter parameter : parameters)
			refreshRowParameter(parameter, drone.parameters);

		// dismiss progress dialog
		if (pd != null) {
			pd.dismiss();
			pd = null;
		}
		
		//Remove the Refresh text view
		refreshTextView.setVisibility(View.GONE);
		
		
	}

	@Override
	public void onParamterMetaDataChanged() {
		drone.parameters.loadMetadata(context, null);
		refresh(drone.parameters);
	}

	private String getMetadataType() {
		if (drone.MavClient.isConnected()) {
			// online: derive from connected vehicle type
			switch (drone.type.getType()) {
			case MAV_TYPE.MAV_TYPE_FIXED_WING: /* Fixed wing aircraft. | */
				return "ArduPlane";

			case MAV_TYPE.MAV_TYPE_GENERIC: /* Generic micro air vehicle. | */
			case MAV_TYPE.MAV_TYPE_QUADROTOR: /* Quadrotor | */
			case MAV_TYPE.MAV_TYPE_COAXIAL: /* Coaxial helicopter | */
			case MAV_TYPE.MAV_TYPE_HELICOPTER: /*
												 * Normal helicopter with tail
												 * rotor. |
												 */
			case MAV_TYPE.MAV_TYPE_HEXAROTOR: /* Hexarotor | */
			case MAV_TYPE.MAV_TYPE_OCTOROTOR: /* Octorotor | */
			case MAV_TYPE.MAV_TYPE_TRICOPTER: /* Octorotor | */
				return "ArduCopter2";

			case MAV_TYPE.MAV_TYPE_GROUND_ROVER: /* Ground rover | */
			case MAV_TYPE.MAV_TYPE_SURFACE_BOAT: /* Surface vessel, boat, ship | */
				return "ArduRover";

				// case MAV_TYPE.MAV_TYPE_ANTENNA_TRACKER: /* Ground
				// installation | */
				// case MAV_TYPE.MAV_TYPE_GCS: /* Operator control unit / ground
				// control station | */
				// case MAV_TYPE.MAV_TYPE_AIRSHIP: /* Airship, controlled | */
				// case MAV_TYPE.MAV_TYPE_FREE_BALLOON: /* Free balloon,
				// uncontrolled | */
				// case MAV_TYPE.MAV_TYPE_ROCKET: /* Rocket | */
				// case MAV_TYPE.MAV_TYPE_SUBMARINE: /* Submarine | */
				// case MAV_TYPE.MAV_TYPE_FLAPPING_WING: /* Flapping wing | */
				// case MAV_TYPE.MAV_TYPE_KITE: /* Flapping wing | */
			default:
				// unsupported
				return null;
			}
		} else {
			// offline: use configured parameter metadata type
			return null;
		}
	}
}
