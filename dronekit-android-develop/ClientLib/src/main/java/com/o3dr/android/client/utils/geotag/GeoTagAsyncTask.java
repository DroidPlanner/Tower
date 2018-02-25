package com.o3dr.android.client.utils.geotag;

import android.os.AsyncTask;

import com.o3dr.android.client.utils.data.tlog.TLogParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * GeoTagAsyncTask images based on camera mavlink messages.
 */
public abstract class GeoTagAsyncTask extends AsyncTask<Void, Integer, GeoTagUtils.ResultObject> {

    private final File rootDir;
    private final List<TLogParser.Event> events;
    private final ArrayList<File> photos;
    private final GeoTagAlgorithm geoTagAlg;

    private final GeoTagUtils.GeoTagListener listener = new GeoTagUtils.GeoTagListener() {
        @Override
        public void onProgress(int numProcessed, int numTotal) {
            publishProgress(numProcessed, numTotal);
        }
    };

    /**
     * Asynchronous method to geotag a list of images using a list of Events as coordinate data.
     *
     * Warning: this copies data to external storage
     *
     * @param rootDir  {@link File}
     * @param events   {@link List<  com.o3dr.android.client.utils.data.tlog.TLogParser.Event>} list of events to geotag photos.
     * @param photos   {@link List<File>} list of files of photos to geotag.
     */
    public GeoTagAsyncTask(File rootDir, List<TLogParser.Event> events, ArrayList<File> photos) {
        this(rootDir, events, photos, new SimpleGeoTagAlgorithm());
    }

    public GeoTagAsyncTask(File rootDir, List<TLogParser.Event> events, ArrayList<File> photos, GeoTagAlgorithm geotagAlg){
        this.rootDir = rootDir;
        this.events = events;
        this.photos = photos;
        this.geoTagAlg = geotagAlg;
    }

    @Override
    protected GeoTagUtils.ResultObject doInBackground(Void... params) {
        if(isCancelled())
            return new GeoTagUtils.ResultObject();

        return GeoTagUtils.geotag(rootDir, events, photos, geoTagAlg, listener);
    }

    @Override
    protected final void onPostExecute(GeoTagUtils.ResultObject resultObject) {
        if (resultObject.didSucceed()) {
            onResult(resultObject.getGeoTaggedPhotos(), resultObject.getFailedFiles());
        } else {
            onFailed(resultObject.getException());
        }
    }

    @Override
    protected final void onProgressUpdate(Integer... values) {
        onProgress(values[0], values[1]);
    }

    @Override
    protected final void onCancelled(GeoTagUtils.ResultObject resultObject) {
        onResult(resultObject.getGeoTaggedPhotos(), resultObject.getFailedFiles());
    }

    /**
     * Callback for successful geotagging
     *
     * @param geoTaggedPhotos {@link HashMap<File, File>} map of files sent in to the geotagged files.
     * @param failedFiles     {@link HashMap<File, Exception>} map of files sent in to exception that occurred when geotagging.
     */
    public abstract void onResult(HashMap<File, File> geoTaggedPhotos, HashMap<File, Exception> failedFiles);

    /**
     * Callback to notify when as items are processed
     *
     * @param numProcessed number of items that have been processed.
     * @param numTotal     total number of items that will be processed for geotagging
     */
    public abstract void onProgress(int numProcessed, int numTotal);

    /**
     * Callback for exception in geotagging
     *
     * @param e {@link Exception}
     */
    public abstract void onFailed(Exception e);


    protected interface GeoTagAlgorithm {
        HashMap<TLogParser.Event, File> match(List<TLogParser.Event> events, ArrayList<File> photos);
    }


}
