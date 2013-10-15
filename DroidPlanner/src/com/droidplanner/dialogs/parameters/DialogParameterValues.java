package com.droidplanner.dialogs.parameters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.droidplanner.R;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterMetadata;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DialogParameterValues {

    public static AlertDialog.Builder build(String name, ParameterMetadata metadata, double value, DialogInterface.OnClickListener listener, Context context) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(name);

        try {
            if(metadata == null || metadata.getValues() == null)
                throw new IllegalArgumentException();

            final Map<Double,String> values = metadata.parseValues();
            if(values.isEmpty())
                throw new IllegalArgumentException();

            int i = 0, checkedItem = -1;
            final String[] items = new String[values.size()];
            for (Map.Entry<Double, String> entry : values.entrySet()) {
                if(entry.getKey() == value)
                    checkedItem = i;
                items[i++] = entry.getValue();
            }

            builder.setSingleChoiceItems(items, checkedItem, listener)
                .setNegativeButton(android.R.string.cancel, null);

        } catch (Throwable ex) {
            builder.setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("Allowed values for this parameter are not available.\nPlease edit the value directly.");
        }

        return builder;
    }

    public static AlertDialog.Builder build(String name, ParameterMetadata metadata, String value, DialogInterface.OnClickListener listener, Context context) {
        try {
            final double dval = Parameter.getFormat().parse(value).doubleValue();
            return build(name, metadata, dval, listener, context);

        } catch (ParseException ex) {
            return new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(name)
                    .setMessage("Value missing or invalid");
        }
    }
}
