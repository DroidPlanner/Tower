package com.droidplanner;

import java.util.List;

import com.droidplanner.MAVLink.ParametersManager.OnParameterManagerListner;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ParametersActivity extends SuperActivity implements OnParameterManagerListner {

	private TextView parameterTable;

	@Override
	int getNavigationItem() {
		return 3;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parameters);
		parameterTable = (TextView) findViewById(R.id.parametersTextView);
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
	public void onParametersReceived(List<String> param) {
		Log.d("PARM", "parameters Received");
		String parameterString = "";
		for (String string : param) {
			parameterString += string + "\n";
		}
		parameterTable.setText(parameterString);
		
	}

}