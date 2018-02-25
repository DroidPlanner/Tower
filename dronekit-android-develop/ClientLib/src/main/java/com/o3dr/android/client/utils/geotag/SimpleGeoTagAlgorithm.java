package com.o3dr.android.client.utils.geotag;

import com.o3dr.android.client.utils.data.tlog.TLogParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Basic Algorithm that traverses backwards, matching Events to photo files
 */
class SimpleGeoTagAlgorithm implements GeoTagAsyncTask.GeoTagAlgorithm {

    @Override
    public HashMap<TLogParser.Event, File> match(List<TLogParser.Event> events, ArrayList<File> photos) {
        HashMap<TLogParser.Event, File> matchedMap = new HashMap<>();

        int eventsSize = events.size();
        int photosSize = photos.size();

        for (int i = eventsSize - 1, j = photosSize - 1; i >= 0 && j >= 0; i--, j--) {
            matchedMap.put(events.get(i), photos.get(j));
        }

        return matchedMap;
    }
}
