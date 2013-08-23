package com.droidplanner.helpers.srtm;

import android.os.AsyncTask;

import com.droidplanner.file.DirectoryPath;
import com.droidplanner.helpers.srtm.Srtm.OnProgressListner;
import com.google.android.gms.maps.model.LatLng;

public class SrtmReader extends AsyncTask<LatLng, String, Integer> implements OnProgressListner {
	public interface OnSrtmReaderListner {
		public void updateSrtmReaderProgress(String values);
		public void srtmReadFinished();
	}
	OnSrtmReaderListner listner;

	public SrtmReader(OnSrtmReaderListner listner) {
		super();
		this.listner = listner;
	}
	
	@Override
	protected Integer doInBackground(LatLng... params) {
		Srtm Srtm = new Srtm(DirectoryPath.getSrtmPath());
		Srtm.setListner(this);
		
		int alt = Srtm.getData(params[0].longitude,params[0].latitude);
		return alt;
	}	
	
	@Override
	public void onProgress(String filename, int percentage) {
		if (percentage>=0) {
			publishProgress("Downloading "+filename+" - "+percentage+"%");			
		}else{
			publishProgress("Downloading "+filename);
		}
	}

	@Override
	protected void onProgressUpdate(String... values) {
		listner.updateSrtmReaderProgress(values[0]);
	}
	@Override
	protected void onPostExecute(Integer result) {
		listner.srtmReadFinished();
	}
}
