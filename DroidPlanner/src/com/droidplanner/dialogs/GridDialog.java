package com.droidplanner.dialogs;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.polygon.GridBuilder;
import com.droidplanner.polygon.Polygon;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.google.android.gms.maps.model.LatLng;

public abstract class GridDialog implements DialogInterface.OnClickListener {
	public abstract void onPolygonGenerated(List<waypoint> list);

	private Polygon polygon;

	private LatLng originPoint;
	private double height;

	private SeekBarWithText distanceView;

	private SeekBarWithText angleView;

	public void generatePolygon(double defaultHatchAngle,
			double defaultHatchDistance, Polygon polygon, LatLng originPoint,
			double height, Context context) {
		this.polygon = polygon;
		this.height = height;
		this.originPoint = originPoint;

		if (!polygon.isValid()) {
			Toast.makeText(context, "Invalid Polygon", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		AlertDialog dialog = buildDialog(context);
		distanceView.setValue(defaultHatchDistance);
		angleView.setValue(defaultHatchAngle);
		dialog.show();
	}

	private AlertDialog buildDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Polygon Generator");

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);

		angleView = new SeekBarWithText(context);
		angleView.setMinMaxInc(0, 180, 0.1);
		angleView.setTitle("Hatch angle:");
		angleView.setUnit("deg");

		distanceView = new SeekBarWithText(context);
		distanceView.setMinMaxInc(5, 500, 5);
		distanceView.setTitle("Distance between lines:");
		distanceView.setUnit("m");

		layout.addView(angleView);
		layout.addView(distanceView);
		builder.setView(layout);

		builder.setNegativeButton("Cancel", this).setPositiveButton("Ok", this);
		AlertDialog dialog = builder.create();
		return dialog;
	}

	@Override
	public void onClick(DialogInterface arg0, int which) {
		if (which == Dialog.BUTTON_POSITIVE) {
			GridBuilder grid = new GridBuilder(polygon, angleView.getValue(),
					distanceView.getValue(), originPoint, height);

			onPolygonGenerated(grid.hatchfill());
		}
	}

}
