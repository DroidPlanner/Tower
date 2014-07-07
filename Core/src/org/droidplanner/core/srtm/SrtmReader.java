package org.droidplanner.core.srtm;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.srtm.Srtm.OnProgressListner;

import android.os.AsyncTask;

public class SrtmReader extends AsyncTask<Integer, String, Integer> implements
		OnProgressListner {
	public interface OnSrtmReaderListner {
		public void updateSrtmReaderProgress(String values);

		public void srtmReadFinished(ArrayList<Integer> altitudes);
	}

	private OnSrtmReaderListner listner;
	private ArrayList<Integer> altitudes = new ArrayList<Integer>();
	private List<Coord2D> path;
	private String srtmPath;

	public SrtmReader(List<Coord2D> path, OnSrtmReaderListner listner, String directoryPath) {
		super();
		this.listner = listner;
		this.path = path;
		this.srtmPath = directoryPath;
	}

	@Override
	protected Integer doInBackground(Integer... params) {
		Srtm Srtm = new Srtm(srtmPath);
		Srtm.setListner(this);
		for (Coord2D latLng : path) {
			altitudes.add(Srtm.getData(latLng.getLng(), latLng.getLat()));
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
