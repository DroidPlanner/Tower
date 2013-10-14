package com.droidplanner.dialogs.parameters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.droidplanner.R;
import com.droidplanner.parameters.ParameterMetadata;


public class DialogParameterInfo {

    public static Dialog build(ParameterMetadata metadata, Context context) {
        return new AlertDialog.Builder(context)
                .setTitle(metadata.getName())
                .setView(buildView(metadata, context))
                .create();
    }

    private static View buildView(ParameterMetadata metadata, Context context) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.dialog_parameter_info, null);

        setTextView(view, R.id.nameView, metadata.getDisplayName());
        setTextView(view, R.id.descView, metadata.getDescription());

        setTextLayout(view, R.id.unitsLayout, R.id.unitsView, metadata.getUnits());
        setTextLayout(view, R.id.rangeLayout, R.id.rangeView, formatRange(metadata.getRange()));
        setTextLayout(view, R.id.valuesLayout, R.id.valuesView, metadata.getValues());

        return view;
    }

    private static String formatRange(String range) {
        if(range == null || range.isEmpty())
            return null;

        final String[] part = range.split(" ");
        if(part.length == 2)
            return part[0] + " - " + part[1];
        else
            return range;
    }

    private static void setTextView(View view, int ridTextView, String text) {
        final TextView textView = (TextView) view.findViewById(ridTextView);
        if(text != null) {
            textView.setText(text);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private static void setTextLayout(View view, int ridLayout, int ridTextView, String text) {
        TextView textView;
        if(text != null) {
            textView = (TextView) view.findViewById(ridTextView);
            textView.setText(text);
        } else {
            view.findViewById(ridLayout).setVisibility(View.GONE);
        }
    }

}
