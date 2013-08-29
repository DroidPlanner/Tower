package com.droidplanner.widgets.tableRow;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidplanner.drone.variables.waypoint;

public class MissionRow extends ArrayAdapter<waypoint> {

	public MissionRow(Context context, int resource, List<waypoint> objects) {
		super(context, resource, objects);
	}

	public MissionRow(Context context, int resource) {
		super(context, resource);
	}

	/*
	 * private List<waypoint> waypoints; private Context context; private
	 * MissionFragment fragment;
	 */


	private TextView nameView;
	private TextView altitudeView;
	private TextView cmdView;
	private Button removeButton;
	private LinearLayout layout;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//createRowViews(parent.getContext());
		// update();
		return super.getView(position, layout, parent);
	}

	private void createRowViews(Context context) {
		layout = new LinearLayout(context);
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
		/*
		 * cmdView.setOnClickListener(this);
		 * altitudeView.setOnClickListener(this);
		 * removeButton.setOnClickListener(this);
		 */

		layout.addView(nameView);
		layout.addView(cmdView);
		layout.addView(altitudeView);
		layout.addView(removeButton);
	}

	/*
	 * 
	 * public void update() {
	 * altitudeView.setText(String.format(Locale.ENGLISH,"%3fm"
	 * ,waypoint.getHeight())); nameView.setText("WP " + waypoint.getNumber());
	 * cmdView.setText(waypoint.getCmd().getName()); }
	 * 
	 * @Override public void onClick(View v) { if (v.equals(removeButton)) {
	 * onDeleteButtonClick(); } else if (v.equals(altitudeView)) {
	 * onAltitudeClick(); } else if (v.equals(cmdView)) { onCmdClick(); } }
	 * 
	 * private void onDeleteButtonClick() { fragment.onDeleteWaypoint(waypoint);
	 * }
	 * 
	 * private void onAltitudeClick() { AltitudeDialog dialog = new
	 * AltitudeDialog(this); dialog.build(waypoint.getHeight(), context); }
	 * 
	 * private void onCmdClick() { final String[] list = new
	 * String[ApmCommands.getNameList().size()];
	 * ApmCommands.getNameList().toArray(list); AlertDialog.Builder dialog = new
	 * AlertDialog.Builder(context); dialog.setTitle("Select type");
	 * dialog.setItems(list, new DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface d, int i) {
	 * waypoint.setCmd(ApmCommands.getCmd(list[i])); update();
	 * fragment.onWaypointUpdate(waypoint); } }); dialog.create().show(); }
	 * 
	 * @Override public void onAltitudeChanged(double newAltitude) {
	 * waypoint.setHeight(newAltitude); update(); }
	 */
}
