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
import com.droidplanner.polygon.Polygon;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.google.android.gms.maps.model.LatLng;

public abstract class GridDialog implements DialogInterface.OnClickListener {
	public abstract void onPolygonGenerated(List<waypoint> list);

	private Polygon polygon;

	private LatLng originPoint;

	private SeekBarWithText distanceView;
	private SeekBarWithText angleView;
	private SeekBarWithText altitudeView;

	public void generatePolygon(double defaultHatchAngle,
			double defaultHatchDistance, Polygon polygon, LatLng originPoint,
			double altitude, Context context) {
		this.polygon = polygon;
		this.originPoint = originPoint;

		if (!polygon.isValid()) {
			Toast.makeText(context, "Invalid Polygon", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		AlertDialog dialog = buildDialog(context);
		distanceView.setValue(defaultHatchDistance);
		angleView.setValue(defaultHatchAngle);
		altitudeView.setValue(altitude);
		dialog.show();
	}

	private AlertDialog buildDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Polygon Generator");

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_grid, null);
		angleView = (SeekBarWithText) layout.findViewById(R.id.angleView);
		distanceView = (SeekBarWithText) layout.findViewById(R.id.distanceView);
		altitudeView = (SeekBarWithText) layout.findViewById(R.id.altitudeView);
		builder.setView(layout);

		builder.setNegativeButton("Cancel", this).setPositiveButton("Ok", this);
		AlertDialog dialog = builder.create();
		return dialog;
	}

	@Override
	public void onClick(DialogInterface arg0, int which) {
		if (which == Dialog.BUTTON_POSITIVE) {
			// TODO make the following code work again by generating a Survey Data object
			/*GridBuilder grid = new GridBuilder(polygon, angleView.getValue(),
					distanceView.getValue(), originPoint,
					altitudeView.getValue());

			onPolygonGenerated(grid.generate());
			*/
		}
	}

}
