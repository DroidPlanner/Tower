package com.droidplanner.activitys;

import java.util.List;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.droidplanner.R;
import com.droidplanner.dialogs.OpenFileDialog;
import com.droidplanner.dialogs.OpenParameterDialog;
import com.droidplanner.fragments.ParametersTableFragment;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterManager.OnParameterManagerListner;
import com.droidplanner.widgets.paramRow.ParamRow;

public class ParametersActivity extends SuperActivity implements
		OnParameterManagerListner {

	private ParametersTableFragment tableFragment;

	@Override
	int getNavigationItem() {
		return 3;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parameters);

		tableFragment = ((ParametersTableFragment)getFragmentManager().findFragmentById(R.id.parametersTable));
		
		app.setOnParametersChangedListner(this);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_load_parameters:
			if (app.MAVClient.isConnected()) {
				app.parameterMananger.getAllParameters();				
			}else{
				Toast.makeText(this, "Please connect first", Toast.LENGTH_SHORT).show();
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
			if (!row.isNewValueEqualToDroneParam()){
				app.parameterMananger.sendParameter(row.getParameterFromRow());
			}						
		}		
		Toast.makeText(this, "Write "+modRows.size()+" parameters", Toast.LENGTH_SHORT).show();		
	}
	
	private void openParametersFromFile() {
		OpenFileDialog dialog = new OpenParameterDialog() {
			@Override
			public void parameterFileLoaded(List<Parameter> parameters) {
				for (Parameter parameter : parameters) {
					onParameterReceived(parameter);
				}				
			}
		};
		dialog.openDialog(this);
	}		
	
	@Override
	public void onParametersReceived() {
		Toast.makeText(this, "Parameters Received", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onParameterReceived(Parameter parameter) {
		tableFragment.refreshRowParameter(parameter);
	}

	
}