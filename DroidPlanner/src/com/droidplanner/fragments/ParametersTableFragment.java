package com.droidplanner.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.Toast;

import com.droidplanner.R;
import com.droidplanner.drone.variables.Parameters;
import com.droidplanner.file.IO.ParameterWriter;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.widgets.tableRow.ParamRow;

public class ParametersTableFragment extends Fragment {

	private TableLayout parameterTable;
	private List<ParamRow> rowList = new ArrayList<ParamRow>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_parameters, container,
				false);
		parameterTable = (TableLayout) view.findViewById(R.id.parametersTable);
		return view;
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
		if((rowList.size() % 2) == 1)
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

	public void saveParametersToFile() {
		List<Parameter> parameterList = getParameterListFromTable();
		if (parameterList.size() > 0) {
			ParameterWriter parameterWriter = new ParameterWriter(parameterList);
			if (parameterWriter.saveParametersToFile()) {
				Toast.makeText(this.getActivity(), "Parameters saved",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

    public void refresh(Parameters parameters) {
        for (ParamRow row : rowList)
            row.setParamMetadata(parameters);
    }
}
