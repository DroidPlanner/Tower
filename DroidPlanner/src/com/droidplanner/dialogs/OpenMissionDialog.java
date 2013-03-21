package com.droidplanner.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import com.MAVLink.Drone;
import com.MAVLink.MissionReader;
import com.droidplanner.R;
import com.droidplanner.helpers.FileManager;

public abstract class OpenMissionDialog implements OnClickListener {
	public abstract void waypointFileLoaded(boolean isFileOpen);

	private String[] itemList;
	Drone drone;
	private Context context;

	public void OpenWaypointDialog(Drone drone, Context context) {
		this.context = context;
		this.drone = drone;

		itemList = FileManager.loadWaypointFileList();
		if (itemList.length == 0) {
			Toast.makeText(context, R.string.no_waypoint_files,
					Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.select_file_to_open);
		dialog.setItems(itemList, this);
		dialog.create().show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {

		MissionReader missionReader = new MissionReader();
		boolean isFileOpen = missionReader.openMission(FileManager.getWaypointsPath()
				+ itemList[which]);	
		
		if (isFileOpen) {
			Toast.makeText(context, itemList[which], Toast.LENGTH_LONG).show();
			drone.home = missionReader.getHome();
			drone.waypoints = missionReader.getWaypoints();
		} else {
			Toast.makeText(context, R.string.error_when_opening_file,
					Toast.LENGTH_SHORT).show();
		}
		
		waypointFileLoaded(isFileOpen);		
	}
}
