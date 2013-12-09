package com.droidplanner.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import com.droidplanner.R;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterMetadata;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2013-12-08
 * Time: 11:00 PM
 */
public class ParamsAdapter extends ArrayAdapter<ParamsAdapterItem> {
    private final int resource;

    private final int colorAltRow;


    public ParamsAdapter(Context context, int resource) {
        this(context, resource, new ArrayList<ParamsAdapterItem>());
    }

    public ParamsAdapter(Context context, int resource, List<ParamsAdapterItem> objects) {
        super(context, resource, objects);

        this. resource = resource;

        colorAltRow = context.getResources().getColor(R.color.paramAltRow);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        final ParamView paramView;

        if(convertView == null) {
            final LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            view = inflater.inflate(resource, parent, false);

            paramView = new ParamView();
            paramView.setNameView((TextView) view.findViewById(R.id.params_row_name));
            paramView.setDescView((TextView) view.findViewById(R.id.params_row_desc));
            paramView.setValueView((EditText) view.findViewById(R.id.params_row_value));
            view.setTag(paramView);

        } else {
            view = convertView;
            paramView = (ParamView) convertView.getTag();

            // detatch text listener
            paramView.getValueView().addTextChangedListener(paramView);
        }

        // populate fields, set appearance
        final ParamsAdapterItem item = getItem(position);
        final Parameter param = item.getParameter();
        final ParameterMetadata metadata = item.getMetadata();

        paramView.setPosition(position);
        paramView.getNameView().setText(param.name);
        paramView.getDescView().setText(getDescription(metadata));
        paramView.getValueView().setText(item.getValue());
        paramView.setAppearance(item);

        // attach listener
        final EditText valueView = paramView.getValueView();
        valueView.addTextChangedListener(paramView);
        valueView.setOnFocusChangeListener(paramView);


        // alternate background color for clarity
        view.setBackgroundColor((position % 2 == 1) ? colorAltRow : Color.TRANSPARENT);

        return view;
    }

    private String getDescription(ParameterMetadata metadata) {
        String desc = "";
        if(metadata != null) {
            // display-name (units)
            desc = metadata.getDisplayName();
            if(metadata.getUnits() != null)
                desc += " (" + metadata.getUnits() + ")";
        }
        return desc;
    }


    private class ParamView implements TextWatcher, View.OnFocusChangeListener {
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
                return Parameter.getFormat().parse(valueView.getText().toString()).doubleValue();
            } catch (ParseException ex) {
                throw new NumberFormatException(ex.getMessage());
            }
        }

        public void setAppearance(ParamsAdapterItem item) {
            final int resid;
            if(item.isDirty()) {
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

            } else {
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
            final ParamsAdapterItem item = getItem(position);
            item.setDirtyValue(editable.toString());

            setAppearance(item);
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if(!hasFocus) {
                // refresh value on leaving view - show results of rounding etc.
                final ParamsAdapterItem item = getItem(position);
                valueView.setText(Parameter.getFormat().format(getValue()));
            }

        }
    }
}
