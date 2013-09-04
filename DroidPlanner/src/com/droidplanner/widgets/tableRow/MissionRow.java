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

	private TextView nameView;
	private TextView altitudeView;
	private TextView typeView;

	public MissionRow(Context context, int resource, List<waypoint> objects) {
		super(context, resource, objects);
		this.waypoints = objects;
		this.context = context;
	}

	public MissionRow(Context context, int resource) {
		super(context, resource);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createRowViews(parent, position);
	}

	private View createRowViews(ViewGroup root, int position) {
		waypoint waypoint = waypoints.get(position);
		View view = createLayoutFromResource();
		findViewObjects(view);		
		setupViewsText(waypoint);
		return view;
	}

	private View createLayoutFromResource() {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.row_mission_list, null);
		return view;
	}

	private void findViewObjects(View view) {
		nameView = (TextView) view.findViewById(R.id.rowNameView);
		altitudeView = (TextView) view.findViewById(R.id.rowAltitudeView);
		typeView = (TextView) view.findViewById(R.id.rowTypeView);
	}

	private void setupViewsText(waypoint waypoint) {
		if (waypoint.getCmd().isNavigation()) {
			altitudeView.setText(String.format(Locale.ENGLISH, "%3.0fm", waypoint.getHeight()));
		} else {
			altitudeView.setText("-");
		}
		nameView.setText(String.format("%3d", waypoint
				.getNumber()));
		typeView.setText(waypoint.getCmd().getName());
	}

}
