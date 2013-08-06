package com.droidplanner.dialogs;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.droidplanner.R;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.polygon.GridBuilder;
import com.droidplanner.polygon.Polygon;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.google.android.gms.maps.model.LatLng;

public abstract class SurveyDialog implements DialogInterface.OnClickListener {
	public abstract void onPolygonGenerated(List<waypoint> list);

	private SeekBarWithText overlapView;
	private SeekBarWithText angleView;
	private SeekBarWithText altitudeView;
	private SeekBarWithText sidelapView;
	
	private Polygon polygon;
	private LatLng originPoint;

	private Double altitude;
	private Double lineDistance;

	public void generateSurveyDialog(Polygon polygon,double defaultHatchAngle, LatLng lastPoint,
			double defaultAltitude, Context context) {
		this.polygon = polygon;
		this.originPoint = lastPoint;

		if (checkIfPolygonIsValid(polygon)) {
			Toast.makeText(context, "Invalid Polygon", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		
		AlertDialog dialog = buildDialog(context);

		sidelapView.setValue(10);
		overlapView.setValue(10);
		angleView.setValue(defaultHatchAngle);
		altitudeView.setValue(defaultAltitude);
		dialog.show();
	}

	private boolean checkIfPolygonIsValid(Polygon polygon) {
		return !polygon.isValid();
	}

	private AlertDialog buildDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Survey");
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_survey, null);
		builder.setView(layout);
		builder.setNegativeButton("Cancel", this).setPositiveButton("Ok", this);
		AlertDialog dialog = builder.create();

		angleView = (SeekBarWithText) layout.findViewById(R.id.angleView);
		overlapView = (SeekBarWithText) layout.findViewById(R.id.overlapView);
		sidelapView = (SeekBarWithText) layout.findViewById(R.id.sidelapView);
		altitudeView = (SeekBarWithText) layout.findViewById(R.id.altitudeView);	

		return dialog;
	}

	@Override
	public void onClick(DialogInterface arg0, int which) {
		if (which == Dialog.BUTTON_POSITIVE) {
			GridBuilder grid = new GridBuilder(polygon, angleView.getValue(),
					lineDistance, originPoint,
					altitude);

			onPolygonGenerated(grid.hatchfill());
		}
	}

}
