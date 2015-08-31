package org.droidplanner.android.view.adapterViews;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.droidplanner.android.R;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.o3dr.services.android.lib.drone.property.Parameter;

/**
 * Date: 2013-12-08 Time: 11:00 PM
 */
public class ParamsAdapter extends FilterableArrayAdapter<ParamsAdapterItem> {

	public interface OnInfoListener {
		void onHelp(int position, EditText valueView);
	}

	public interface OnParametersChangeListener {
		void onParametersChange(int dirtyCount);
	}

    private final static DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance();
    static {
        formatter.applyPattern("0.###");
    }

    private int dirtyCount = 0;
	private final int resource;
	private final int colorAltRow;

    private final LayoutInflater mInflater;

	private View focusView;
	private OnInfoListener onInfoListener;
	private OnParametersChangeListener onParametersChangeListener;

	public ParamsAdapter(Context context, int resource) {
		this(context, resource, new ArrayList<ParamsAdapterItem>());
	}

	public ParamsAdapter(Context context, int resource, List<ParamsAdapterItem> objects) {
		super(context, resource, objects);

		this.resource = resource;
		colorAltRow = context.getResources().getColor(R.color.paramAltRow);
        mInflater = LayoutInflater.from(context);
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

	public void setOnParametersChangeListener(OnParametersChangeListener onParametersChangeListener){
		this.onParametersChangeListener = onParametersChangeListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view;
		final ParamTag paramTag;

		if (convertView == null) {
			// create new view
			view = mInflater.inflate(resource, parent, false);

			paramTag = new ParamTag();
            paramTag.setContainerView(view);
			paramTag.setNameView((TextView) view.findViewById(R.id.params_row_name));
			paramTag.setDescView((TextView) view.findViewById(R.id.params_row_desc));
			paramTag.setValueView((EditText) view.findViewById(R.id.params_row_value));
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

		paramTag.setPosition(position);
		paramTag.getNameView().setText(param.getName());
		paramTag.getDescView().setText(getDescription(param));
		paramTag.setAppearance(item);

		final EditText valueView = paramTag.getValueView();
		valueView.setText(param.getDisplayValue());

		// attach listeners
        view.setOnClickListener(paramTag);
		valueView.addTextChangedListener(paramTag);
		valueView.setOnFocusChangeListener(paramTag);

		return view;
	}

    public void updateParameters(Map<String, Parameter> parameters){
        if(parameters == null || parameters.isEmpty())
            return;

        final int parametersCount = getCount();
        for(int i = 0; i < parametersCount; i++){
            ParamsAdapterItem item = getItem(i);
            Parameter update = parameters.remove(item.getParameter().getName());
            if(update != null){
                boolean dirtyValue = item.isDirty();
                item.setDirtyValue(update.getDisplayValue());
                if(dirtyValue && !item.isDirty()){
                    dirtyCount--;
                }else if(!dirtyValue && item.isDirty()){
                    dirtyCount++;
                }
            }
        }

        if(!parameters.isEmpty()){
            for(Map.Entry<String, Parameter> entry : parameters.entrySet()){
                addParameter(entry.getKey(), entry.getValue(), true);
            }
        }

        notifyDataSetChanged();
    }
    
	public void loadParameters(Map<String, Parameter> parameters) {
		clear();
		for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
            addParameter(entry.getKey(), entry.getValue());
        }
        dirtyCount = 0;
	}

    private void addParameter(String name, Parameter parameter) {
        addParameter(name, parameter, false);
    }

    private void addParameter(String name, Parameter parameter, boolean isDirty){
        try {
            if(isDirty){
                dirtyCount++;
            }
            ParamsAdapterItem item = new ParamsAdapterItem(parameter);
            item.setDirtyValue(parameter.getDisplayValue(), isDirty);
            add(item);
        } catch (Exception ex) {
            // eat it
        }
    }


	private String getDescription(Parameter parameter) {
		String desc = "";
		if (parameter != null) {
			// display-name (units)
			desc = parameter.getDisplayName();
			if (parameter.getUnits() != null)
				desc += " (" + parameter.getUnits() + ")";
		}
		return desc;
	}

	private void clearFocus(View view) {
		if (view != null) {
			view.clearFocus();

			final InputMethodManager inputMethodManager = (InputMethodManager) getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	private class ParamTag implements TextWatcher, View.OnFocusChangeListener, View.OnClickListener {
		private int position;
		private TextView nameView;
		private TextView descView;
		private EditText valueView;
        private View containerView;

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
				return formatter.parse(valueView.getText().toString()).doubleValue();
			} catch (ParseException ex) {
				// invalid number, return 0
				return 0;
			}
		}

		public void setAppearance(ParamsAdapterItem item) {
			final int resid;
			if (item.isDirty()) {
				final ParamsAdapterItem.Validation validation = item.getValidation();
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

                containerView.setBackgroundResource(R.drawable.dirty_params_row_bg);
			} else {
                containerView.setBackgroundResource(R.drawable.params_row_bg);
				resid = R.style.paramValueUnchanged;
			}
			valueView.setTextAppearance(getContext(), resid);
		}

		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			// nop
		}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			// nop
		}

		@Override
		public void afterTextChanged(Editable editable) {
			// During reload text may change as valueView looses focus
			// after underlying data has been invalidated - avoid this
			if (position >= getCount())
				return;

			final ParamsAdapterItem item = getItem(position);
            boolean dirtyValue = item.isDirty();
			item.setDirtyValue(editable.toString());
            if(dirtyValue && !item.isDirty()){
                dirtyCount--;
            }else if(!dirtyValue && item.isDirty()){
                dirtyCount++;
            }
			if(onParametersChangeListener != null) {
				onParametersChangeListener.onParametersChange(dirtyCount);
            }

			setAppearance(item);
		}

		@Override
		public void onFocusChange(View view, boolean hasFocus) {
			if (!hasFocus) {
				// refresh value on leaving view - show results of rounding etc.
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

        public void setContainerView(View view) {
            containerView = view;
        }
    }
}
