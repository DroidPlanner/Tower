package org.droidplanner.android.dialogs.openfile;

import org.droidplanner.android.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public abstract class OpenFileDialog implements OnClickListener {

	public interface FileReader {
		String getPath();

		String[] getFileList();

		boolean openFile(String file);
	}

	protected abstract FileReader createReader();

	protected abstract void onDataLoaded(FileReader reader);

	private String[] itemList;
	protected Context context;
	private FileReader reader;
    private String selectedFilename;

	public void openDialog(Context context) {
		this.context = context;
		reader = createReader();

		itemList = reader.getFileList();
		if (itemList.length == 0) {
			Toast.makeText(context, R.string.no_files, Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.select_file_to_open);
		dialog.setItems(itemList, this);
		dialog.create().show();
	}

    public String getSelectedFilename(){
        return this.selectedFilename;
    }

	@Override
	public void onClick(DialogInterface dialog, int which) {
        this.selectedFilename = itemList[which];
        final String filename = reader.getPath() + this.selectedFilename;
        new OpenFileAsyncTask(this).execute(filename);
	}

    private static class OpenFileAsyncTask extends AsyncTask<String, Void, Boolean> {

        private final WeakReference<OpenFileDialog> containerRef;
        private final ProgressDialog progressDialog;

        public OpenFileAsyncTask(OpenFileDialog dialog){
            containerRef = new WeakReference<>(dialog);
            progressDialog = new ProgressDialog(dialog.context);
            progressDialog.setTitle("Processing...");
            progressDialog.setMessage("Please wait.");
            progressDialog.setIndeterminate(true);
        }

        @Override
        protected void onPreExecute(){
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            final OpenFileDialog ref = containerRef.get();
            if(ref == null) return Boolean.FALSE;

            final String filename = params[0];

            boolean isFileOpen = ref.reader.openFile(filename);
            return isFileOpen;
        }

        @Override
        protected void onCancelled(){
            if(progressDialog.isShowing())
                progressDialog.dismiss();
        }

        @Override
        protected void onPostExecute(Boolean result){
            final OpenFileDialog ref = containerRef.get();
            if(ref != null){
                if (result) {
                    Toast.makeText(ref.context, R.string.status_file_opened, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ref.context, R.string.error_when_opening_file,
                            Toast.LENGTH_SHORT).show();
                }

                ref.onDataLoaded(ref.reader);
            }

            if(progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

}