package com.droidplanner.widgets.tableRow;

import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.MAVLink.Messages.ApmCommands;
import com.droidplanner.dialogs.AltitudeDialog;
import com.droidplanner.dialogs.AltitudeDialog.OnAltitudeChangedListner;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.MissionFragment;

public class MissionRow extends TableRow implements OnClickListener,
		OnAltitudeChangedListner {

	private Context context;
	private MissionFragment fragment;
	private waypoint waypoint;

	private TextView nameView;
	private TextView altitudeView;
	private TextView cmdView;
	private Button removeButton;

	public MissionRow(Context context) {
		super(context);
	}

	public MissionRow(MissionFragment missionFragment, waypoint waypoint) {
		super(missionFragment.getActivity());
		this.fragment = missionFragment;
		this.context = missionFragment.getActivity();
		this.waypoint = waypoint;
		createRowViews(context);
		update();
	}

	public void update() {
		altitudeView.setText(String.format(Locale.ENGLISH,"%3fm",waypoint.getHeight()));
		nameView.setText("WP " + waypoint.getNumber());
		cmdView.setText(waypoint.getCmd().getName());
	}

	private void createRowViews(Context context) {
		nameView = new TextView(context);
		altitudeView = new TextView(context);
		cmdView = new TextView(context);
		removeButton = new Button(context);
		removeButton.setText("X");

		// Sizes
		nameView.setWidth(50);
		cmdView.setWidth(60);
		altitudeView.setWidth(80);

		// Listeners
		cmdView.setOnClickListener(this);
		altitudeView.setOnClickListener(this);
		removeButton.setOnClickListener(this);

		addView(nameView);
		addView(cmdView);
		addView(altitudeView);
		addView(removeButton);
	}

	@Override
	public void onClick(View v) {
		if (v.equals(removeButton)) {
			onDeleteButtonClick();
		} else if (v.equals(altitudeView)) {
			onAltitudeClick();
		} else if (v.equals(cmdView)) {
			onCmdClick();
		}
	}

	private void onDeleteButtonClick() {
		fragment.onDeleteWaypoint(waypoint);
	}

	private void onAltitudeClick() {
		AltitudeDialog dialog = new AltitudeDialog(this);
		dialog.build(waypoint.getHeight(), context);
	}

	private void onCmdClick() {
		final String[] list = new String[ApmCommands.getNameList().size()];
		ApmCommands.getNameList().toArray(list);
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle("Select type");
		dialog.setItems(list, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface d, int i) {
				waypoint.setCmd(ApmCommands.getCmd(list[i]));
				update();
				fragment.onWaypointUpdate(waypoint);
			}
		});
		dialog.create().show();
	}

	@Override
	public void onAltitudeChanged(double newAltitude) {
		waypoint.setHeight(newAltitude);
		update();
	}
}
