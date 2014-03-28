package org.droidplanner.android.dialogs.openfile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import org.droidplanner.R;

public abstract class OpenFileDialog implements OnClickListener {

	public interface FileReader {
		public String getPath();

		public String[] getFileList();

		public boolean openFile(String file);
	}

	protected abstract FileReader createReader();

	protected abstract void onDataLoaded(FileReader reader);

	private String[] itemList;
	private Context context;
	private FileReader reader;

	public void openDialog(Context context) {
		this.context = context;
		reader = createReader();

		itemList = reader.getFileList();
		if (itemList.length == 0) {
			Toast.makeText(context, R.string.no_files, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.select_file_to_open);
		dialog.setItems(itemList, this);
		dialog.create().show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		boolean isFileOpen = reader
				.openFile(reader.getPath() + itemList[which]);

		if (isFileOpen) {
			Toast.makeText(context, itemList[which], Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, R.string.error_when_opening_file,
					Toast.LENGTH_SHORT).show();
		}

		onDataLoaded(reader);
	}

}