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
import com.MAVLink.Messages.enums.MAV_CMD;

public class MissionRow extends ArrayAdapter<waypoint> {

	private Context context;
	private List<waypoint> waypoints;


	private TextView nameView;
	private TextView altitudeView;
	private TextView typeView;
	private TextView descView;



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
		descView = (TextView) view.findViewById(R.id.rowDescView);

	}

	private void setupViewsText(waypoint waypoint) {
		if (waypoint.getCmd().isNavigation()) {
			altitudeView.setText(String.format(Locale.ENGLISH, "%3.0fm", waypoint.getHeight()));
		} else {
			altitudeView.setText("-");
		}
		
		nameView.setText(String.format("%3d", waypoint.getNumber()));
		typeView.setText(waypoint.getCmd().getName());
		descView.setText(setupDescription(waypoint));
	}

	private String setupDescription(waypoint waypoint) {
		String descStr = null;
		float tmpVal;
		descStr = "";
		
		switch(waypoint.getCmd().getType())
		{
		case MAV_CMD.MAV_CMD_NAV_WAYPOINT:
			descStr += "- ";
			descStr += context.getString(R.string.waypointDesc_Waypoint);
			descStr += " ";
			if(waypoint.missionItem.param2<=0){
				descStr += context.getString(R.string.waypointDesc_immediate);
			} 
			else{
				descStr += context.getString(R.string.waypointDesc_after);
				descStr += String.format(Locale.ENGLISH, " %1.0f", waypoint.missionItem.param2);
				descStr += context.getString(R.string.waypointDesc_s);
			}
			break;
		case MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM:
			tmpVal = waypoint.missionItem.param3<0?-1*waypoint.missionItem.param3:waypoint.missionItem.param3;			
			descStr += context.getString(R.string.waypointDesc_Loiter);
			descStr += " ";
			descStr += String.format(Locale.ENGLISH, "%1.1f", tmpVal);
			descStr += context.getString(R.string.waypointDesc_m);
			descStr += " ";
			descStr += context.getString(R.string.waypointDesc_radius);
			descStr += " ";
			descStr += waypoint.missionItem.param3<0?context.getString(R.string.waypointDesc_CCW):context.getString(R.string.waypointDesc_CW);
			break;
		case MAV_CMD.MAV_CMD_NAV_LOITER_TURNS:
			tmpVal = waypoint.missionItem.param3<0?-1*waypoint.missionItem.param3:waypoint.missionItem.param3;			
			descStr += context.getString(R.string.waypointDesc_LoiterN);
			descStr += " ";
			descStr += String.format(Locale.ENGLISH, "%1.0f ", waypoint.missionItem.param1);
			descStr += context.getString(R.string.waypointDesc_turns);
			descStr += " ";
			descStr += String.format(Locale.ENGLISH, "within %1.1f", tmpVal);
			descStr += context.getString(R.string.waypointDesc_m);
			descStr += " ";
			descStr += context.getString(R.string.waypointDesc_radius);
			descStr += " ";
			descStr += waypoint.missionItem.param3<0?context.getString(R.string.waypointDesc_CCW):context.getString(R.string.waypointDesc_CW);
			break;
		case MAV_CMD.MAV_CMD_NAV_LOITER_TIME:
			tmpVal = waypoint.missionItem.param3<0?-1*waypoint.missionItem.param3:waypoint.missionItem.param3;			
			descStr += context.getString(R.string.waypointDesc_LoiterT);
			descStr += " ";
			descStr += String.format(Locale.ENGLISH, "%1.0f", waypoint.missionItem.param1);
			descStr += context.getString(R.string.waypointDesc_s);
			descStr += " ";
			descStr += String.format(Locale.ENGLISH, "within %1.1f", tmpVal);
			descStr += context.getString(R.string.waypointDesc_m);
			descStr += " ";
			descStr += context.getString(R.string.waypointDesc_radius);
			descStr += " ";
			descStr += waypoint.missionItem.param3<0?context.getString(R.string.waypointDesc_CCW):context.getString(R.string.waypointDesc_CW);
			break;
		case MAV_CMD.MAV_CMD_NAV_TAKEOFF:
			descStr += context.getString(R.string.waypointDesc_Takeoff);
			descStr += " ";
			descStr += String.format(Locale.ENGLISH, "%1.2f", waypoint.missionItem.param1);
			descStr += context.getString(R.string.waypointDesc_degrees);
			break;
		}
		
		return descStr;
	}
}
