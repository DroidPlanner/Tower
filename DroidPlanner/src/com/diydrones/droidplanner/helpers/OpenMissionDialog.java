package com.diydrones.droidplanner.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import com.diydrones.droidplanner.R;
import com.diydrones.droidplanner.waypoints.MissionManager;

public abstract class OpenMissionDialog implements OnClickListener {
	public abstract void waypointFileLoaded(boolean isFileOpen);

	private String[] itemList;
	MissionManager mission;
	private Context context;

	public void OpenWaypointDialog(MissionManager mission, Context context) {
		this.context = context;
		this.mission = mission;

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
		boolean isFileOpen = mission.openMission(FileManager.getWaypointsPath()
				+ itemList[which]);
		
		if (isFileOpen) {
			Toast.makeText(context, itemList[which], Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, R.string.error_when_opening_file,
					Toast.LENGTH_SHORT).show();
		}
		
		waypointFileLoaded(isFileOpen);		
	}
}
