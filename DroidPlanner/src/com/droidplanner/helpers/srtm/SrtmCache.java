package com.droidplanner.helpers.srtm;

import com.droidplanner.file.DirectoryPath;

import android.os.AsyncTask;
import android.util.Log;

public class SrtmCache extends AsyncTask<Integer, Integer, Integer>{

	@Override
	protected Integer doInBackground(Integer... params) {
		String path = DirectoryPath.getDroidPlannerPath()+"/SRTM/";
		int alt = SRTM.getData(-51.1439127,-29.7026708,path); //Near my house
		int sea = SRTM.getData(-50.0360209,-29.8055343,path); //Sea level	
		int high = SRTM.getData(-50.0360209,-20.8055343,path); //High place
		
		Log.d("SRTM","Altitude:"+alt+" Sea:"+sea+ " High:"+high);
		
		return null;
	}
	
}
