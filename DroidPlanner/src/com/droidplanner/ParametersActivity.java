package com.droidplanner;

import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;

import com.droidplanner.MAVLink.Parameter;
import com.droidplanner.MAVLink.ParametersManager.OnParameterManagerListner;
import com.droidplanner.widgets.paramRow.ParamRow;

public class ParametersActivity extends SuperActivity implements
		OnParameterManagerListner {

	private TableLayout parameterTable;

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
			app.parameterMananger.getWaypoints();
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

	@Override
	public void onParametersReceived(List<Parameter> parameters) {
		Log.d("PARM", "parameters Received");
		parameterTable.removeAllViews();
		for (Parameter param : parameters) {
			ParamRow pRow = new ParamRow(this);
			pRow.setParam(param);			
			parameterTable.addView(pRow);
		}
	}


}