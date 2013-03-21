package com.droidplanner.dialogs;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import com.droidplanner.R;
import com.droidplanner.helpers.FileManager;
import com.droidplanner.helpers.KmlParser;
import com.droidplanner.waypoints.gcp;

public abstract class OpenGcpFileDialog implements OnClickListener{
	public abstract void onGcpFileLoaded(List<gcp> gcpList);

	Context context;
	String[] itemList;
	
	public void openGCPDialog(Context context) {
		this.context = context;
		itemList = FileManager.loadKMZFileList();
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
	
	public void onClick(DialogInterface dialog, int which) {
		KmlParser parser = new KmlParser();
		boolean fileIsOpen = parser.openGCPFile(FileManager.getGCPPath() + itemList[which]);
		if(fileIsOpen) {			
			Toast.makeText(context, itemList[which],
					Toast.LENGTH_LONG).show();
			onGcpFileLoaded(parser.gcpList);
		} else {
			Toast.makeText(context,
					R.string.error_when_opening_file,
					Toast.LENGTH_SHORT).show();
			onGcpFileLoaded(null);
		}
	}
}
