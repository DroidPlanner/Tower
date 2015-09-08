package org.droidplanner.android.dialogs.parameters;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.droidplanner.android.R;
import org.droidplanner.android.view.adapterViews.ParamsAdapterItem;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.o3dr.services.android.lib.drone.property.Parameter;

public class DialogParameterInfo {

    private final static DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance();
    static {
        formatter.applyPattern("0.###");
    }

	public static AlertDialog build(ParamsAdapterItem item, EditText valueView, Context context) {
		final View view = buildView(item, context);

		final AlertDialog dialog = new AlertDialog.Builder(context).setView(view).create();
        dialog.setCanceledOnTouchOutside(true);

		// spinner's onItemSelcted impl needs ref to Dialog interface
		buildValueSpinner(dialog, view, item.getParameter(), valueView, context);

		return dialog;
	}

	private static View buildView(ParamsAdapterItem item, Context context) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.fragment_parameters_info, null);

		final Parameter parameter = item.getParameter();
		setTextView(view, R.id.displayNameView, parameter.getDisplayName());
		setTextView(view, R.id.nameView, parameter.getName());
		setTextView(view, R.id.descView, parameter.getDescription());

		setTextLayout(view, R.id.unitsLayout, R.id.unitsView, parameter.getUnits());
		setTextLayout(view, R.id.rangeLayout, R.id.rangeView, formatRange(parameter.getRange()));

		setTextLayout(view, R.id.valuesLayout, R.id.valuesView, parameter.getValues());

		return view;
	}

	private static String formatRange(String range) {
		if (range == null || range.isEmpty())
			return null;

		final String[] part = range.split(" ");
		if (part.length == 2)
			return part[0] + "  /  " + part[1];
		else
			return range;
	}

	private static void setTextView(View view, int ridTextView, String text) {
		final TextView textView = (TextView) view.findViewById(ridTextView);
		if (text != null) {
			textView.setText(text);
		} else {
			textView.setVisibility(View.GONE);
		}
	}

	private static void setTextLayout(View view, int ridLayout, int ridTextView, String text) {
		if (text != null) {
			final TextView textView = (TextView) view.findViewById(ridTextView);
			textView.setText(text);
		} else {
			view.findViewById(ridLayout).setVisibility(View.GONE);
		}
	}

	private static void buildValueSpinner(final DialogInterface dialogInterface, View view,
			Parameter parameter, final EditText valueView, Context context) {
		// bail if nothing to do
		if (parameter.getValues() == null)
			return;

		try {
			final Map<Double, String> valueMap = parameter.parseValues();
			final List<Double> values = new ArrayList<Double>(valueMap.keySet());
			final List<String> strings = new ArrayList<String>();

			// get current dirty value
			final double dirtyValue = formatter.parse(valueView.getText().toString()).doubleValue();

			// build value / string collections
			int position = values.indexOf(dirtyValue);
			if (position == -1) {
				// not found: add 'custom value'
				position = 0;
				values.add(position, dirtyValue);
				strings.add(String.format("%s: %s", formatter.format(dirtyValue), context
						.getResources().getString(R.string.metadata_custom_value)));
			}
			for (Map.Entry<Double, String> entry : valueMap.entrySet())
				strings.add(String.format("%s: %s", formatter.format(entry.getKey()),
						entry.getValue()));

			// build adapter
			final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
					R.layout.spinner_param_value_item, strings);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			// setup spinner
			final Spinner valueSpinner = (Spinner) view.findViewById(R.id.valueSpinner);
			valueSpinner.setAdapter(adapter);
			valueSpinner.setSelection(position);
			valueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				boolean once = true;

				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int position,
						long id) {
					valueView.setText(formatter.format(values.get(position)));
					if (once)
						once = false;
					else
						dialogInterface.dismiss();
				}

				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {
					// nop
				}
			});

		} catch (Exception e) {
			// can't populate spinner - remove it
			view.findViewById(R.id.valueSpinnerView).setVisibility(View.GONE);
			view.findViewById(R.id.valueTextView).setVisibility(View.VISIBLE);
		}
	}
}
