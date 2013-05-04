package com.droidplanner.dialogs;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import com.droidplanner.R;
import com.droidplanner.MAVLink.Drone;
import com.droidplanner.MAVLink.parameters.Parameter;
import com.droidplanner.MAVLink.parameters.ParameterReader;
import com.droidplanner.helpers.FileManager;

public abstract class OpenParameterDialog implements OnClickListener {
	public abstract void parameterFileLoaded(List<Parameter> parameters);

	private String[] itemList;
	private Context context;

	public void OpenWaypointDialog(Drone drone, Context context) {
		this.context = context;

		itemList = FileManager.loadParametersFileList();
		if (itemList.length == 0) {
			Toast.makeText(context, R.string.no_files,
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

		ParameterReader reader = new ParameterReader();
		boolean isFileOpen = reader.openFile(FileManager.getParametersPath()
				+ itemList[which]);	
		
		if (isFileOpen) {
			Toast.makeText(context, itemList[which], Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, R.string.error_when_opening_file,
					Toast.LENGTH_SHORT).show();
		}
		
		parameterFileLoaded(reader.getParameters());		
	}
}
