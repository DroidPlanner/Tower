package com.droidplanner.fragments.mode;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import com.droidplanner.R;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterMetadata;

/**
 * User: rgayle
 * Date: 2013-12-08
 * Time: 11:00 PM
 */
public class ParamsAdapter extends ArrayAdapter<ParamsAdapter.ParameterWithMetadata> {
    private final int resource;

    public ParamsAdapter(Context context, int resource) {
        super(context, resource);

        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        final ParamRow paramRow;

        if(convertView == null) {
            final LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            view = inflater.inflate(resource, parent, false);

            paramRow = new ParamRow();
            paramRow.setName((TextView) view.findViewById(R.id.params_row_name));
            paramRow.setDesc((TextView) view.findViewById(R.id.params_row_desc));
            paramRow.setValue((EditText) view.findViewById(R.id.params_row_value));
            view.setTag(paramRow);

        } else {
            view = convertView;
            paramRow = (ParamRow) convertView.getTag();
        }

        // populate fields
        final ParameterWithMetadata pwm = getItem(position);
        final Parameter param = pwm.getParameter();
        final ParameterMetadata metadata = pwm.getMetadata();

        paramRow.getName().setText(param.name);
        paramRow.getDesc().setText(getDescription(metadata));
        paramRow.getValue().setText(param.getValue());

        // alternate background color for clarity
        view.setBackgroundColor((position % 2 == 1) ? Color.rgb(0xF0, 0xF0, 0xF0) : Color.TRANSPARENT);

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


    public static class ParameterWithMetadata {
        private Parameter parameter;
        private ParameterMetadata metadata;

        public ParameterWithMetadata(Parameter parameter, ParameterMetadata metadata) {
            this.parameter = parameter;
            this.metadata = metadata;
        }

        public Parameter getParameter() {
            return parameter;
        }

        public void setParameter(Parameter parameter) {
            this.parameter = parameter;
        }

        public ParameterMetadata getMetadata() {
            return metadata;
        }

        public void setMetadata(ParameterMetadata metadata) {
            this.metadata = metadata;
        }
    }


    private static class ParamRow {
        private TextView name;
        private TextView desc;
        private EditText value;

        public TextView getName() {
            return name;
        }

        public void setName(TextView name) {
            this.name = name;
        }

        public TextView getDesc() {
            return desc;
        }

        public void setDesc(TextView desc) {
            this.desc = desc;
        }

        public EditText getValue() {
            return value;
        }

        public void setValue(EditText value) {
            this.value = value;
        }
    }
}
