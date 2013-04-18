package com.droidplanner;

import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.droidplanner.MAVLink.Parameter;
import com.droidplanner.MAVLink.ParametersManager.OnParameterManagerListner;

public class ParametersActivity extends SuperActivity implements
		OnParameterManagerListner, OnClickListener {

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
			TableRow row = new TableRow(this);
			TextView nameView = new TextView(this);
			TextView valueView = new TextView(this);
			TextView typeView = new TextView(this);
			TextView indexView = new TextView(this);
			Button	sendButton = new Button(this);
			sendButton.setOnClickListener(this);
			
			nameView.setText(param.name);
			valueView.setText(String.format("%3.3f",param.value));
			typeView.setText(Integer.toString(param.type));
			indexView.setText(Integer.toString(param.index));
			sendButton.setText("Send");
			
			nameView.setWidth(150);
			valueView.setWidth(100);
			typeView.setWidth(50);
			indexView.setWidth(50);
			
			row.addView(nameView);
			row.addView(valueView);
			row.addView(typeView);
			row.addView(indexView);
			row.addView(sendButton);
			
			parameterTable.addView(row);
		}
	}

	@Override
	public void onClick(View view) {
		String id = (String) ((TextView)((TableRow)view.getParent()).getChildAt(0)).getText();
		Log.d("PARM", "Send: "+id);
	}

}