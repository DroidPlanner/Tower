package org.droidplanner.android.dialogs.openfile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import android.widget.Toast;

import com.o3dr.android.client.utils.FileUtils;

import org.droidplanner.android.R;
import org.droidplanner.android.utils.file.FileList;

import java.io.File;
import java.util.TreeMap;

public abstract class OpenFileDialog {

    public abstract void onFileSelected(String filepath);

	public void openDialog(Activity activity, final String rootPath, final String[] fileList) {
		if (fileList == null || fileList.length == 0) {
			Toast.makeText(activity, R.string.no_files, Toast.LENGTH_SHORT).show();
			return;
		}

        final TreeMap<String, String> filteredFiles = new TreeMap<>();
		for(String filename : fileList){
            String filenameWithoutExt = FileUtils.getFilenameWithoutExtension(filename);
            String prevFilename = filteredFiles.get(filenameWithoutExt);
            if(TextUtils.isEmpty(prevFilename)){
                filteredFiles.put(filenameWithoutExt, filename);
            } else{
                // Prefer the filename with the FileList.WAYPOINT_FILENAME_EXT extension
                if(FileList.WAYPOINT_FILENAME_EXT.equals(FileUtils.getFileExtension(filename))){
                    filteredFiles.put(filenameWithoutExt, filename);
                }
            }
		}

        final String[] fileLabels = filteredFiles.keySet().toArray(new String[filteredFiles.size()]);

		AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
		dialog.setTitle(R.string.select_file_to_open);
		dialog.setItems(fileLabels, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filepath = rootPath + File.separator + filteredFiles.get(fileLabels[which]);
                onFileSelected(filepath);
            }
        });
		dialog.create().show();
	}

}