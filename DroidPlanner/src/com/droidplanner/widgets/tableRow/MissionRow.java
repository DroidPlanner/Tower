package com.droidplanner.widgets.tableRow;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.droidplanner.R;
import com.droidplanner.drone.variables.waypoint;

public class MissionRow extends ArrayAdapter<waypoint> {

	private Context context;
	private List<waypoint> waypoints;

	public MissionRow(Context context, int resource, List<waypoint> objects) {
		super(context, resource, objects);
		this.waypoints = objects;
		this.context = context;
	}

	public MissionRow(Context context, int resource) {
		super(context, resource);
		this.context = context;
	}

	/*
	 * private List<waypoint> waypoints; private Context context; private
	 * MissionFragment fragment;
	 */

	private TextView nameView;
	private TextView altitudeView;
	private TextView typeView;
	//private Button removeButton;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createRowViews(parent,position);
	}

	private View createRowViews(ViewGroup root, int position) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.mission_list_row, null);
		nameView = (TextView) view.findViewById(R.id.rowNameView);
		altitudeView = (TextView) view.findViewById(R.id.rowAltitudeView);
		typeView = (TextView) view.findViewById(R.id.rowTypeView);

		altitudeView.setText(String.format(Locale.ENGLISH, "%3.0fm",
				waypoints.get(position).getHeight()));
		nameView.setText(String.format("%3d", waypoints.get(position).getNumber()));
		typeView.setText(waypoints.get(position).getCmd().getName());
		
		// removeButton = new Button(context);
		// removeButton.setText("X");

		// Listeners
		/*
		 * cmdView.setOnClickListener(this);
		 * altitudeView.setOnClickListener(this);
		 * removeButton.setOnClickListener(this);
		 */
		return view;
	}

	public void update() {
	}
	/*
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
