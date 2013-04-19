package com.droidplanner.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;

public abstract class AltitudeDialog implements DialogInterface.OnClickListener {
	public abstract void onAltitudeChanged(double newAltitude);

	private SeekBarWithText altitudeView;
	
	public void build(double defaultAltitude, Context context) {
		AlertDialog dialog = buildDialog(context);
		altitudeView.setValue(defaultAltitude);
		dialog.show();
	}

	private AlertDialog buildDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Altitude");
		
		altitudeView = new SeekBarWithText(context);
		altitudeView.setMinMaxInc(0, 9999, 2);
		altitudeView.setTitle("Flight Height:");
		altitudeView.setUnit("m");
		
		builder.setView(altitudeView);
		
		builder.setNegativeButton("Cancel", this).setPositiveButton("Ok", this);
		AlertDialog dialog = builder.create();
		return dialog;
	}


	@Override
	public void onClick(DialogInterface arg0, int which) {
		if (which == Dialog.BUTTON_POSITIVE) {
			onAltitudeChanged(altitudeView.getValue());
		}
	}

}
