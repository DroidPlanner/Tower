package org.droidplanner.android.dialogs.openfile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import org.droidplanner.android.R;

import java.io.File;

public abstract class OpenFileDialog {

    public abstract void onFileSelected(String filepath);

	public void openDialog(Activity activity, final String rootPath, final String[] fileList) {
		if (fileList == null || fileList.length == 0) {
			Toast.makeText(activity, R.string.no_files, Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
		dialog.setTitle(R.string.select_file_to_open);
		dialog.setItems(fileList, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filepath = rootPath + File.separator + fileList[which];
                onFileSelected(filepath);
            }
        });
		dialog.create().show();
	}

}