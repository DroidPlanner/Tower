package com.droidplanner.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.Toast;

import com.droidplanner.R;
import com.droidplanner.MAVLink.parameters.Parameter;
import com.droidplanner.MAVLink.parameters.ParameterWriter;
import com.droidplanner.widgets.paramRow.ParamRow;

public class ParametersTableFragment extends Fragment {
	

	private TableLayout parameterTable;
	private List<ParamRow> rowList = new ArrayList<ParamRow>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.parameters_fragment, container, false);
		parameterTable = (TableLayout) view.findViewById(R.id.parametersTable);
		return view;
	}

	public void refreshRowParameter(Parameter parameter) {
		try {
			Parameter.checkParameterName(parameter.name);
			ParamRow row = findRowByName(parameter.name);
			if (row != null) {
				row.setParam(parameter);
			} else {
				addParameterRow(parameter);
			}
		} catch (Exception e) {
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

	private void addParameterRow(Parameter param){
		ParamRow pRow = new ParamRow(this.getActivity());
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
	
	public List<ParamRow> getModifiedParametersRows(){
		ArrayList<ParamRow> modParameters = new ArrayList<ParamRow>();
		for (ParamRow row : rowList) {
			if (!row.isNewValueEqualToDroneParam()){
				modParameters.add(row);
			}
		}
		return modParameters;
	}
	
	public void saveParametersToFile() {
		List<Parameter> parameterList = getParameterListFromTable();
		if (parameterList.size()>0) {
			ParameterWriter parameterWriter = new ParameterWriter(parameterList);
			if(parameterWriter.saveParametersToFile()){
				Toast.makeText(this.getActivity(), "Parameters saved", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

}
