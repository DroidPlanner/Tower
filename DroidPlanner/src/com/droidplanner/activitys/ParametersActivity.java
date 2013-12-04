package com.droidplanner.activitys;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.dialogs.openfile.OpenFileDialog;
import com.droidplanner.dialogs.openfile.OpenParameterDialog;
import com.droidplanner.drone.DroneInterfaces;
import com.droidplanner.fragments.ParametersTableFragment;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.widgets.tableRow.ParamRow;

public class ParametersActivity extends SuperActivity implements
		DroneInterfaces.OnParameterManagerListner, DroneInterfaces.VehicleTypeListener {

	private ParametersTableFragment tableFragment;
	private ProgressDialog pd;

	@Override
	public int getNavigationItem() {
		return 3;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parameters);

		tableFragment = ((ParametersTableFragment) getFragmentManager()
				.findFragmentById(R.id.parametersTable));

		drone.parameters.parameterListner = this;
        drone.setVehicleTypeListener(this);

		Toast.makeText(this, "Touch REFRESH to read parameters", Toast.LENGTH_LONG)
				.show();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_load_parameters:
			if (drone.MavClient.isConnected()) {
				drone.parameters.getAllParameters();
			} else {
				Toast.makeText(this, "Please connect first", Toast.LENGTH_SHORT)
						.show();
			}
			return true;
		case R.id.menu_save_parameters:
			tableFragment.saveParametersToFile();
			return true;
		case R.id.menu_open_parameters:
			openParametersFromFile();
			return true;
		case R.id.menu_write_parameters:
			writeModifiedParametersToDrone();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_parameters, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void writeModifiedParametersToDrone() {
		List<ParamRow> modRows = tableFragment.getModifiedParametersRows();
		for (ParamRow row : modRows) {
			if (!row.isNewValueEqualToDroneParam()) {
				drone.parameters.sendParameter(row.getParameterFromRow());
			}
		}
		Toast.makeText(this, "Write " + modRows.size() + " parameters",
				Toast.LENGTH_SHORT).show();
	}

	private void openParametersFromFile() {
		OpenFileDialog dialog = new OpenParameterDialog() {
			@Override
			public void parameterFileLoaded(List<Parameter> parameters) {
				Collections.sort(parameters, new Comparator<Parameter>()
				{
					@Override
					public int compare(Parameter p1, Parameter p2)
					{
						return p1.name.compareTo(p2.name);
					}
				});
                drone.parameters.loadMetadata();
				for (Parameter parameter : parameters)
                    tableFragment.refreshRowParameter(parameter, drone.parameters);
			}
		};
		dialog.openDialog(this);
	}

    @Override
	public void onBeginReceivingParameters() {
		pd = new ProgressDialog(this);
		pd.setTitle("Refreshing Parameters...");
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setIndeterminate(true);
		pd.setCancelable(false);
		pd.setCanceledOnTouchOutside(true);

		pd.show();
	}

	@Override
	public void onParameterReceived(Parameter parameter, int index, int count) {
		if(pd != null) {
			if(pd.isIndeterminate()) {
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
		for(Parameter parameter : parameters)
            tableFragment.refreshRowParameter(parameter, drone.parameters);

		// dismiss progress dialog
		if(pd != null) {
			pd.dismiss();
			pd = null;
		}
	}

    @Override
    public void onVehicleTypeChanged() {
        drone.parameters.loadMetadata();
        tableFragment.refresh(drone.parameters);
    }
}