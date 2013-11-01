package com.droidplanner.dialogs;

import com.droidplanner.helpers.units.Altitude;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.NumberPicker;

public class AltitudeDialog implements DialogInterface.OnClickListener {
	private NumberPicker thousandPicker;
	private NumberPicker hundredPicker;
	private NumberPicker decadePicker;
	private NumberPicker unitPicker;
	private OnAltitudeChangedListner listner;

	public interface OnAltitudeChangedListner {
		public void onAltitudeChanged(Altitude newAltitude);
	}

	public AltitudeDialog(OnAltitudeChangedListner listner) {
		this.listner = listner;
	}

	public void build(Altitude altitude, Context context) {
		AlertDialog dialog = buildDialog(context);
		setValue(altitude.valueInMeters());
		dialog.show();
	}

	private AlertDialog buildDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Altitude");
		builder.setView(buildAltitudePicker(context));
		builder.setNegativeButton("Cancel", this).setPositiveButton("Ok", this);
		AlertDialog dialog = builder.create();
		return dialog;
	}

	private View buildAltitudePicker(Context context) {
		LayoutParams layoutStyle = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		layoutStyle.weight = 1;
		LinearLayout layout = new LinearLayout(context);
		layout.setLayoutParams(layoutStyle);

		unitPicker = buildDigitPicker(context, layoutStyle);
		decadePicker = buildDigitPicker(context, layoutStyle);
		hundredPicker = buildDigitPicker(context, layoutStyle);
		thousandPicker = buildDigitPicker(context, layoutStyle);

		layout.addView(thousandPicker);
		layout.addView(hundredPicker);
		layout.addView(decadePicker);
		layout.addView(unitPicker);
		return layout;
	}

	private NumberPicker buildDigitPicker(Context context,
			LayoutParams layoutStyle) {
		NumberPicker digitPicker = new NumberPicker(context);
		digitPicker.setMaxValue(9);
		digitPicker.setMinValue(0);
		digitPicker
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		digitPicker.setLayoutParams(layoutStyle);
		digitPicker.setWrapSelectorWheel(false);
		return digitPicker;
	}

	@Override
	public void onClick(DialogInterface arg0, int which) {
		if (which == Dialog.BUTTON_POSITIVE) {
			listner.onAltitudeChanged(new Altitude(getValue()));
		}
	}

	private void setValue(double value) {
		thousandPicker.setValue((int) (value / 1000));
		value -= thousandPicker.getValue() * 1000;
		hundredPicker.setValue((int) (value / 100));
		value -= hundredPicker.getValue() * 100;
		decadePicker.setValue((int) (value / 10));
		value -= decadePicker.getValue() * 10;
		unitPicker.setValue((int) (value));
	}

	private double getValue() {
		return (thousandPicker.getValue() * 1000 + hundredPicker.getValue()
				* 100 + decadePicker.getValue() * 10 + unitPicker.getValue());
	}

}
