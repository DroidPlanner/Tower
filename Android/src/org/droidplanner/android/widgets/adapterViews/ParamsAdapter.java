package org.droidplanner.android.widgets.adapterViews;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.droidplanner.R;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.android.utils.file.IO.ParameterMetadataMapReader;
import org.droidplanner.android.widgets.adapterViews.ParamsAdapterItem.Validation;
import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.core.parameters.ParameterMetadata;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Date: 2013-12-08 Time: 11:00 PM
 */
public class ParamsAdapter extends ArrayAdapter<ParamsAdapterItem> {

	public interface OnInfoListener {
		void onHelp(int position, EditText valueView);
	}

	private static final DecimalFormat formatter = Parameter.getFormat();

	private final int resource;
	private final int colorAltRow;

	private Map<String, ParameterMetadata> metadataMap;

	private View focusView;
	private OnInfoListener onInfoListener;

	public ParamsAdapter(Context context, int resource) {
		this(context, resource, new ArrayList<ParamsAdapterItem>());
	}

	public ParamsAdapter(Context context, int resource,
			List<ParamsAdapterItem> objects) {
		super(context, resource, objects);

		this.resource = resource;

		colorAltRow = context.getResources().getColor(R.color.paramAltRow);
	}

	public void clearFocus() {
		if (focusView != null) {
			clearFocus(focusView);
			focusView = null;
		}
	}

	public void setOnInfoListener(OnInfoListener onInfoListener) {
		this.onInfoListener = onInfoListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view;
		final ParamTag paramTag;

		if (convertView == null) {
			// create new view
			final LayoutInflater inflater = ((Activity) getContext())
					.getLayoutInflater();
			view = inflater.inflate(resource, parent, false);

			paramTag = new ParamTag();
			paramTag.setNameView((TextView) view
					.findViewById(R.id.params_row_name));
			paramTag.setDescView((TextView) view
					.findViewById(R.id.params_row_desc));
			paramTag.setValueView((EditText) view
					.findViewById(R.id.params_row_value));
			view.setTag(paramTag);

		} else {
			// recycle view
			view = convertView;
			paramTag = (ParamTag) convertView.getTag();

			// remove focus
			final EditText valueView = paramTag.getValueView();
			if (valueView.hasFocus())
				clearFocus(valueView);

			// detatch listeners
			valueView.removeTextChangedListener(paramTag);
			valueView.setOnFocusChangeListener(null);
		}

		// populate fields, set appearance
		final ParamsAdapterItem item = getItem(position);
		final Parameter param = item.getParameter();
		final ParameterMetadata metadata = item.getMetadata();

		paramTag.setPosition(position);
		paramTag.getNameView().setText(param.name);
		paramTag.getDescView().setText(getDescription(metadata));
		paramTag.setAppearance(item);

		final EditText valueView = paramTag.getValueView();
		valueView.setText(param.getValue());

		// attach listeners
		paramTag.getNameView().setOnClickListener(paramTag);
		paramTag.getDescView().setOnClickListener(paramTag);
		valueView.addTextChangedListener(paramTag);
		valueView.setOnFocusChangeListener(paramTag);

		// alternate background color for clarity
		view.setBackgroundColor((position % 2 == 1) ? colorAltRow
				: Color.TRANSPARENT);

		return view;
	}

	public void loadParameters(Drone drone, List<Parameter> parameters) {
		loadMetadataInternal(drone);

		clear();
		for (Parameter parameter : parameters)
			addParameter(parameter);
	}

	private void addParameter(Parameter parameter) {
		try {
			Parameter.checkParameterName(parameter.name);
			add(new ParamsAdapterItem(parameter, getMetadata(parameter.name)));

		} catch (Exception ex) {
			// eat it
		}
	}

	public void loadMetadata(Drone drone) {
		loadMetadataInternal(drone);

		for (int i = 0; i < getCount(); i++) {
			final ParamsAdapterItem item = getItem(i);
			item.setMetadata(getMetadata(item.getParameter().name));
		}
		notifyDataSetChanged();
	}

	private void loadMetadataInternal(Drone drone) {
		metadataMap = null;

		// get metadata type from profile, bail if none
		final String metadataType;
		final VehicleProfile profile = drone.profile.getProfile();
		if (profile == null
				|| (metadataType = profile.getParameterMetadataType()) == null)
			return;

		try {
			// load
			metadataMap = ParameterMetadataMapReader.load(getContext(),
					metadataType);

		} catch (Exception ex) {
			// nop
		}
	}

	private ParameterMetadata getMetadata(String name) {
		return (metadataMap == null) ? null : metadataMap.get(name);
	}

	private String getDescription(ParameterMetadata metadata) {
		String desc = "";
		if (metadata != null) {
			// display-name (units)
			desc = metadata.getDisplayName();
			if (metadata.getUnits() != null)
				desc += " (" + metadata.getUnits() + ")";
		}
		return desc;
	}

	private void clearFocus(View view) {
		if (view != null) {
			view.clearFocus();

			final InputMethodManager inputMethodManager = (InputMethodManager) getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager
					.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	private class ParamTag implements TextWatcher, View.OnFocusChangeListener,
			View.OnClickListener {
		private int position;
		private TextView nameView;
		private TextView descView;
		private EditText valueView;

		public void setPosition(int position) {
			this.position = position;
		}

		public TextView getNameView() {
			return nameView;
		}

		public void setNameView(TextView nameView) {
			this.nameView = nameView;
		}

		public TextView getDescView() {
			return descView;
		}

		public void setDescView(TextView descView) {
			this.descView = descView;
		}

		public EditText getValueView() {
			return valueView;
		}

		public void setValueView(EditText valueView) {
			this.valueView = valueView;
		}

		public double getValue() {
			try {
				return formatter.parse(valueView.getText().toString())
						.doubleValue();
			} catch (ParseException ex) {
				// invalid number, return 0
				return 0;
			}
		}

		public void setAppearance(ParamsAdapterItem item) {
			final int resid;
			if (item.isDirty()) {
				final ParamsAdapterItem.Validation validation = item
						.getValidation();
				switch (validation) {
				case VALID:
					resid = R.style.paramValueValid;
					break;
				case INVALID:
					resid = R.style.paramValueInvalid;
					break;
				default:
					resid = R.style.paramValueChanged;
					break;
				}

			} else {
				resid = R.style.paramValueUnchanged;
			}
			valueView.setTextAppearance(getContext(), resid);
		}

		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i2,
				int i3) {
			// nop
		}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i2,
				int i3) {
			// nop
		}

		@Override
		public void afterTextChanged(Editable editable) {
			// During reload text may change as valueView looses focus
			// after underlying data has been evalidated - avoid this
			if (position >= getCount())
				return;

			final ParamsAdapterItem item = getItem(position);
			item.setDirtyValue(editable.toString());

			setAppearance(item);
		}

		@Override
		public void onFocusChange(View view, boolean hasFocus) {
			if (!hasFocus) {
				// refresh value on leaving view - show results of rounding etc.
				valueView.setText(formatter.format(getValue()));
				focusView = null;

			} else {
				focusView = view;
			}

		}

		@Override
		public void onClick(View view) {
			clearFocus();

			if (onInfoListener != null)
				onInfoListener.onHelp(position, valueView);
		}
	}
}
