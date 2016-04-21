package org.droidplanner.android.maps.providers.google_map.tiles.offline;

import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.offline.MapDownloader;

public interface MapDownloaderListener {

    void stateChanged(MapDownloader.OfflineMapDownloaderState newState);
    void initialCountOfFiles(int numberOfFiles);
    void progressUpdate(int numberOfFilesWritten, int numberOfFilesExcepted);
    void networkConnectivityError(Throwable error);
    void sqlLiteError(Throwable error);
    void httpStatusError(int status, String url);
    void completionOfOfflineDatabaseMap();

}
