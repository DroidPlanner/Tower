package com.droidplanner.helpers.srtm;

import android.os.AsyncTask;
import android.util.Log;

import com.droidplanner.file.DirectoryPath;
import com.droidplanner.helpers.srtm.Srtm.OnProgressListner;

public class SrtmCache extends AsyncTask<Integer, Integer, Integer> implements OnProgressListner {

	@Override
	protected Integer doInBackground(Integer... params) {
		Srtm Srtm = new Srtm(DirectoryPath.getSrtmPath());
		Srtm.setListner(this);
		int alt = 0, sea = 0, high = 0;

		Log.d("SRTM", "fetching data");
		alt = Srtm.getData(-51.1439127, -29.7026708); // Near my house
		sea = Srtm.getData(-50.0360209, -29.8055343); // Sea level
		high = Srtm.getData(-50.0360209, -20.8055343); // High place
		Log.d("SRTM", "Altitude:" + alt + " Sea:" + sea + " High:" + high);
		return null;
	}
	
	
	@Override
	public void onProgress(String filename, int percentage) {
		if (percentage>=0) {
			Log.d("SRTM", "Downloading "+filename+" - "+percentage+"%");			
		}else{
			Log.d("SRTM", "Downloading "+filename);
		}
	}	

}
