package com.droidplanner.dialogs;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.MAVLink.waypoint;
import com.droidplanner.R;
import com.droidplanner.waypoints.Polygon;
import com.google.android.gms.maps.model.LatLng;

public abstract class PolygonDialog implements DialogInterface.OnClickListener,
		OnSeekBarChangeListener {
	public abstract void onPolygonGenerated(List<waypoint> list);

	private Context context;
	private Polygon polygon;

	private Double hatchAngle;
	private Double hatchDistance;

	private SeekBar angleSeekBar;
	private SeekBar distanceSeekBar;
	private TextView angleTextView;
	private TextView distanceTextView;
	private LatLng originPoint;
	private double height;

	public void generatePolygon(double defaultHatchAngle,
			double defaultHatchDistance, Polygon polygon, LatLng originPoint,
			double height, Context context) {
		this.context = context;
		this.polygon = polygon;
		this.height = height;
		this.originPoint = originPoint;
		hatchAngle = defaultHatchAngle;
		hatchDistance = defaultHatchDistance;

		if (!polygon.isValid()) {
			Toast.makeText(context, "Invalid Polygon", Toast.LENGTH_SHORT).show();
			return;
		}

		AlertDialog dialog = buildDialog(context);
		dialog.show();
		initializeSeekBars(dialog);
	}

	private AlertDialog buildDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Polygon Generator");
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		builder.setView(inflater.inflate(R.layout.dialog_polygon, null));
		builder.setNegativeButton("Cancel", this).setPositiveButton("Ok", this);
		AlertDialog dialog = builder.create();
		return dialog;
	}

	private void initializeSeekBars(AlertDialog dialog) {
		angleSeekBar = (SeekBar) dialog.findViewById(R.id.SeekBarAngle);
		angleTextView = (TextView) dialog.findViewById(R.id.TextViewAngle);
		distanceSeekBar = (SeekBar) dialog.findViewById(R.id.seekBarDistance);
		distanceTextView = (TextView) dialog
				.findViewById(R.id.textViewDistance);

		angleSeekBar.setOnSeekBarChangeListener(this);
		distanceSeekBar.setOnSeekBarChangeListener(this);
		angleSeekBar.setProgress(hatchAngle.intValue());
		distanceSeekBar.setProgress(hatchDistance.intValue());
	}

	@Override
	public void onClick(DialogInterface arg0, int which) {
		if (which == Dialog.BUTTON_POSITIVE) {
			onPolygonGenerated(polygon.hatchfill(hatchAngle, hatchDistance,
					originPoint, height));
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (seekBar == angleSeekBar) {
			hatchAngle = (double) progress;
			angleTextView.setText(context.getResources().getString(
					R.string.dialog_polygon_hatch_angle)
					+ "\t" + hatchAngle + "º");
		} else if (seekBar == distanceSeekBar) {
			hatchDistance = (double) progress;
			distanceTextView.setText(context.getResources().getString(
					R.string.dialog_polygon_distance_between_lines)
					+ "\t" + hatchDistance + "m");
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

}
