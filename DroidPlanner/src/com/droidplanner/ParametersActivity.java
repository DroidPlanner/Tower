package com.droidplanner;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.Toast;

import com.droidplanner.MAVLink.parameters.Parameter;
import com.droidplanner.MAVLink.parameters.ParameterWriter;
import com.droidplanner.MAVLink.parameters.ParametersManager.OnParameterManagerListner;
import com.droidplanner.widgets.paramRow.ParamRow;

public class ParametersActivity extends SuperActivity implements
		OnParameterManagerListner {

	private TableLayout parameterTable;
	private List<ParamRow> rowList = new ArrayList<ParamRow>();

	@Override
	int getNavigationItem() {
		return 3;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parameters);
		parameterTable = (TableLayout) findViewById(R.id.parametersTable);
		app.setOnParametersChangedListner(this);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_load_parameters:
			app.parameterMananger.getAllParameters();
			return true;
		case R.id.menu_save_parameters:
			saveParametersToFile();
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
		int count =0;
		for (ParamRow row : rowList) {
			if (!row.isNewValueEqualToDroneParam()){
				app.parameterMananger.sendParameter(row.getParameterFromRow());
				count++;
			}						
		}		
		Toast.makeText(this, "Write "+count+" parameters", Toast.LENGTH_SHORT).show();		
	}
	
	@Override
	public void onParametersReceived() {
		Log.d("PARM", "parameters Received");
		Toast.makeText(this, "Parameters Received", Toast.LENGTH_LONG).show();
	}


	@Override
	public void onParameterReceived(Parameter parameter) {
		ParamRow row = findRowByName(parameter.name);
		if (row!=null) {
			row.setParam(parameter);	
		}else{
			addParameterRow(parameter);
		}
	}

	private ParamRow findRowByName(String name) {
		for (ParamRow row : rowList) {
			if(row.getParamName().equals(name)){
				return row;
			}				
		}
		return null;
	}

	private void addParameterRow(Parameter param) {
		ParamRow pRow = new ParamRow(this);
		pRow.setParam(param);			
		rowList.add(pRow);
		parameterTable.addView(pRow);
	}
	
	private List<Parameter> getParameterListFromTable(){
		ArrayList<Parameter> parameters = new ArrayList<Parameter>();
		for (ParamRow row : rowList) {
			parameters.add(row.getParameterFromRow());
		}
		return parameters;
	}

	private void saveParametersToFile() {
		List<Parameter> parameterList = getParameterListFromTable();
		if (parameterList.size()>0) {
			ParameterWriter parameterWriter = new ParameterWriter(parameterList);
			if(parameterWriter.saveParametersToFile()){
				Toast.makeText(this, "Parameters saved", Toast.LENGTH_SHORT).show();
			}
		}
	}
}