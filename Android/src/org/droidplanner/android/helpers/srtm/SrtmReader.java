package org.droidplanner.android.helpers.srtm;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.android.helpers.srtm.Srtm.OnProgressListner;
import org.droidplanner.android.utils.file.DirectoryPath;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

public class SrtmReader extends AsyncTask<Integer, String, Integer> implements
		OnProgressListner {
	public interface OnSrtmReaderListner {
		public void updateSrtmReaderProgress(String values);

		public void srtmReadFinished(ArrayList<Integer> altitudes);
	}

	private OnSrtmReaderListner listner;
	private ArrayList<Integer> altitudes = new ArrayList<Integer>();
	private List<LatLng> path;

	public SrtmReader(List<LatLng> path, OnSrtmReaderListner listner) {
		super();
		this.listner = listner;
		this.path = path;
	}

	@Override
	protected Integer doInBackground(Integer... params) {
		Srtm Srtm = new Srtm(DirectoryPath.getSrtmPath());
		Srtm.setListner(this);
		for (LatLng latLng : path) {
			altitudes.add(Srtm.getData(latLng.longitude, latLng.latitude));
		}
		return 0;
	}

	@Override
	public void onProgress(String filename, int percentage) {
		if (percentage >= 0) {
			publishProgress("Downloading " + filename + " - " + percentage
					+ "%");
		} else {
			publishProgress("Downloading " + filename);
		}
	}

	@Override
	protected void onProgressUpdate(String... values) {
		listner.updateSrtmReaderProgress(values[0]);
	}

	@Override
	protected void onPostExecute(Integer result) {
		listner.srtmReadFinished(altitudes);
	}

}
