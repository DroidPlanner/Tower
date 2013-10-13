package com.droidplanner.activitys;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.MAVLink.Messages.enums.MAV_TYPE;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.dialogs.openfile.OpenFileDialog;
import com.droidplanner.dialogs.openfile.OpenParameterDialog;
import com.droidplanner.drone.DroneInterfaces;
import com.droidplanner.fragments.ParametersTableFragment;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.widgets.tableRow.ParamRow;

public class ParametersActivity extends SuperActivity implements
		DroneInterfaces.OnParameterManagerListner {

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
                drone.parameters.loadMetaData(ParametersActivity.this, null);
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
        Collections.sort(parameters, new Comparator<Parameter>()
        {
            @Override
            public int compare(Parameter p1, Parameter p2)
            {
                return p1.name.compareTo(p2.name);
            }
        });
        drone.parameters.loadMetaData(this, getMetadataType());
		for(Parameter parameter : parameters)
            tableFragment.refreshRowParameter(parameter, drone.parameters);

		// dismiss progress dialog
		if(pd != null) {
			pd.dismiss();
			pd = null;
		}
	}

    @Override
    public void onParamterMetaDataChanged() {
        drone.parameters.loadMetaData(this, null);
        tableFragment.refresh(drone.parameters);
    }

    private String getMetadataType() {
        if (drone.MavClient.isConnected()) {
            // online: derive from connected vehicle type
            switch(drone.type.getType()) {
                case MAV_TYPE.MAV_TYPE_FIXED_WING: /* Fixed wing aircraft. | */
                    return "ArduPlane";

                case MAV_TYPE.MAV_TYPE_GENERIC: /* Generic micro air vehicle. | */
                case MAV_TYPE.MAV_TYPE_QUADROTOR: /* Quadrotor | */
                case MAV_TYPE.MAV_TYPE_COAXIAL: /* Coaxial helicopter | */
                case MAV_TYPE.MAV_TYPE_HELICOPTER: /* Normal helicopter with tail rotor. | */
                case MAV_TYPE.MAV_TYPE_HEXAROTOR: /* Hexarotor | */
                case MAV_TYPE.MAV_TYPE_OCTOROTOR: /* Octorotor | */
                case MAV_TYPE.MAV_TYPE_TRICOPTER: /* Octorotor | */
                    return "ArduCopter2";

                case MAV_TYPE.MAV_TYPE_GROUND_ROVER: /* Ground rover | */
                case MAV_TYPE.MAV_TYPE_SURFACE_BOAT: /* Surface vessel, boat, ship | */
                    return "ArduRover";

//                case MAV_TYPE.MAV_TYPE_ANTENNA_TRACKER: /* Ground installation | */
//                case MAV_TYPE.MAV_TYPE_GCS: /* Operator control unit / ground control station | */
//                case MAV_TYPE.MAV_TYPE_AIRSHIP: /* Airship, controlled | */
//                case MAV_TYPE.MAV_TYPE_FREE_BALLOON: /* Free balloon, uncontrolled | */
//                case MAV_TYPE.MAV_TYPE_ROCKET: /* Rocket | */
//                case MAV_TYPE.MAV_TYPE_SUBMARINE: /* Submarine | */
//                case MAV_TYPE.MAV_TYPE_FLAPPING_WING: /* Flapping wing | */
//                case MAV_TYPE.MAV_TYPE_KITE: /* Flapping wing | */
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