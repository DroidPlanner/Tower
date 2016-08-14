package org.droidplanner.android.dialogs.openfile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.o3dr.services.android.lib.drone.property.Parameter;

import org.droidplanner.android.R;
import org.droidplanner.android.utils.file.IO.ParameterReader;

import java.lang.ref.WeakReference;
import java.util.List;

public abstract class OpenParameterDialog extends OpenFileDialog {

    private Activity activity;

    @Override
    public void openDialog(Activity activity, final String rootPath, final String[] fileList) {
        this.activity = activity;
        super.openDialog(activity, rootPath, fileList);
    }

	public abstract void parameterFileLoaded(String filename, List<Parameter> parameters);

	@Override
	public void onFileSelected(String filepath){
		new OpenFileAsyncTask(this, filepath).execute();
	}

	private static class OpenFileAsyncTask extends AsyncTask<String, Void, Boolean> {

		private final WeakReference<OpenParameterDialog> containerRef;
		private final ProgressDialog progressDialog;
		private final ParameterReader reader;
        private final String filepath;

		public OpenFileAsyncTask(OpenParameterDialog dialog, String filepath){
			containerRef = new WeakReference<>(dialog);
			this.reader = new ParameterReader();
            this.filepath = filepath;
			progressDialog = new ProgressDialog(dialog.activity);
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

			boolean isFileOpen = reader.openFile(filepath);
			return isFileOpen;
		}

		@Override
		protected void onCancelled(){
			if(progressDialog.isShowing())
				progressDialog.dismiss();
		}

		@Override
		protected void onPostExecute(Boolean result){
			final OpenParameterDialog ref = containerRef.get();
			if(ref != null){
				if (result) {
					Toast.makeText(ref.activity, R.string.status_file_opened, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(ref.activity, R.string.error_when_opening_file,
							Toast.LENGTH_SHORT).show();
				}

                ref.parameterFileLoaded(filepath, reader.getParameters());
			}

			if(progressDialog.isShowing())
				progressDialog.dismiss();
		}
	}
}