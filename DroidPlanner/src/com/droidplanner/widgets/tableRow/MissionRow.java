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
		if (waypoint.getCmd().showOnMap()) {
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
		String tmpStr = null;
		float tmpVal;
		descStr = "";
		
		switch(waypoint.getCmd().getType())
		{
		case MAV_CMD.MAV_CMD_NAV_WAYPOINT:
			if(waypoint.missionItem.param2<=0){
				descStr = String.format(Locale.ENGLISH, context.getString(R.string.waypointDesc_Waypoint_1),
						waypoint.missionItem.param4);
			} 
			else{
				descStr = String.format(Locale.ENGLISH, context.getString(R.string.waypointDesc_Waypoint_2),
						waypoint.missionItem.param2,waypoint.missionItem.param4);
			}
			break;

		case MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM:
			tmpVal = waypoint.missionItem.param3<0?-1*waypoint.missionItem.param3:waypoint.missionItem.param3;			
			tmpStr = waypoint.missionItem.param3<0?context.getString(R.string.waypointDesc_CCW):context.getString(R.string.waypointDesc_CW);
			descStr += String.format(Locale.ENGLISH, context.getString(R.string.waypointDesc_Loiter), 
					tmpVal,tmpStr,waypoint.missionItem.param4);
			break;

		case MAV_CMD.MAV_CMD_NAV_LOITER_TURNS:
			tmpVal = waypoint.missionItem.param3<0?-1*waypoint.missionItem.param3:waypoint.missionItem.param3;			
			tmpStr = waypoint.missionItem.param3<0?context.getString(R.string.waypointDesc_CCW):context.getString(R.string.waypointDesc_CW);
			descStr += String.format(Locale.ENGLISH, context.getString(R.string.waypointDesc_LoiterN), 
					waypoint.missionItem.param1,tmpVal,tmpStr,waypoint.missionItem.param4);
			break;

		case MAV_CMD.MAV_CMD_NAV_LOITER_TIME:
			tmpVal = waypoint.missionItem.param3<0?-1*waypoint.missionItem.param3:waypoint.missionItem.param3;			
			tmpStr = waypoint.missionItem.param3<0?context.getString(R.string.waypointDesc_CCW):context.getString(R.string.waypointDesc_CW);
			descStr += String.format(Locale.ENGLISH, context.getString(R.string.waypointDesc_LoiterT), 
					waypoint.missionItem.param1,tmpVal,tmpStr,waypoint.missionItem.param4);
			break;

		case MAV_CMD.MAV_CMD_NAV_TAKEOFF:
			descStr += String.format(Locale.ENGLISH, context.getString(R.string.waypointDesc_Takeoff), waypoint.missionItem.param1);
			break;

		case MAV_CMD.MAV_CMD_CONDITION_CHANGE_ALT:
			descStr += String.format(Locale.ENGLISH, context.getString(R.string.waypointDesc_CondAlt),
					waypoint.missionItem.z,waypoint.missionItem.param1);
			break;

		case MAV_CMD.MAV_CMD_CONDITION_DISTANCE:
			descStr += String.format(Locale.ENGLISH, context.getString(R.string.waypointDesc_CondDist),
					waypoint.missionItem.param1);
			break;

		case MAV_CMD.MAV_CMD_CONDITION_YAW:
			tmpStr = waypoint.missionItem.param4>0?context.getString(R.string.waypoint_yawrelative):
				context.getString(R.string.waypoint_yawabsolute);
			descStr += String.format(Locale.ENGLISH, context.getString(R.string.waypointDesc_CondYaw),
					waypoint.missionItem.param1,
					waypoint.missionItem.param2,
					waypoint.missionItem.param3>0?context.getString(R.string.waypointDesc_CCW):context.getString(R.string.waypointDesc_CW),
					tmpStr);
			break;

		case MAV_CMD.MAV_CMD_DO_SET_HOME:
			descStr += context.getString(R.string.waypointDesc_SetHome);
			descStr += " ";
			if(waypoint.missionItem.param1>0){
				descStr += context.getString(R.string.waypointDesc_coordmav);
			}
			else {
				switch(waypoint.homeType){
				case 0:
					descStr += context.getString(R.string.waypointDesc_coordwp);
					break;
				case 1:
					descStr += context.getString(R.string.waypointDesc_coordgcs);
					break;
				case 2:
					descStr += context.getString(R.string.waypointDesc_coordmanual);
					break;
				default:
					break;
				}
				descStr +=" ";
				descStr += String.format(Locale.ENGLISH, context.getString(R.string.waypointDesc_GPS), 
						waypoint.getCoord().latitude,waypoint.getCoord().longitude);
				break;
			}
	
			break;

		case MAV_CMD.MAV_CMD_DO_CHANGE_SPEED:
			tmpStr = waypoint.missionItem.param1>0?"Ground Speed":"Air Speed";
			descStr += String.format(Locale.ENGLISH, context.getString(R.string.waypointDesc_SetSpeed),
					waypoint.missionItem.param2,waypoint.missionItem.param3, tmpStr);
			break;

		case MAV_CMD.MAV_CMD_DO_SET_RELAY:
			descStr += context.getString(R.string.waypointDesc_SetRelay);
			descStr += " ";
			break;

		case MAV_CMD.MAV_CMD_DO_REPEAT_RELAY:
			descStr += context.getString(R.string.waypointDesc_SetRepeat);
			descStr += " ";
			break;

		case MAV_CMD.MAV_CMD_DO_JUMP:
			descStr += context.getString(R.string.waypointDesc_SetJump);
			descStr += " ";
			break;
		}
		
		return descStr;
	}
}
