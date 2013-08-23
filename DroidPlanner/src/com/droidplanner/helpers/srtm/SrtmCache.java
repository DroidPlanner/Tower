package com.droidplanner.helpers.srtm;

import android.os.AsyncTask;

import com.droidplanner.file.DirectoryPath;
import com.droidplanner.helpers.srtm.Srtm.OnProgressListner;
import com.google.android.gms.maps.model.LatLng;

public abstract class SrtmCache extends AsyncTask<LatLng, String, Integer> implements OnProgressListner {
	public abstract void update(String values);
	public abstract void finish();

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
		update(values[0]);
	}
	@Override
	protected void onPostExecute(Integer result) {
		finish();
	}
}
