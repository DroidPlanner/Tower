package com.o3dr.android.client.utils.geotag;

import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_camera_feedback;
import com.MAVLink.common.msg_named_value_int;
import com.o3dr.android.client.utils.data.tlog.TLogParser;
import com.o3dr.android.client.utils.data.tlog.TLogParser.Event;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Fredia Huya-Kouadio on 1/7/16.
 */
public class FilterByTimestampGeoTagAlgorithm implements GeoTagAsyncTask.GeoTagAlgorithm{

    private final static String TAG = FilterByTimestampGeoTagAlgorithm.class.getSimpleName();

    /**
     * The list of events must start with an event whose mavlink message is a msg_named_value_int.
     * That msg_named_value_int contains the start time and label for the mission whose data we are geotagging.
     *
     * The mission start time, and end time (max event timestamp > start time) will be used to filter the media data set.
     *
     * @param events
     * @param photos
     * @return
     */
    @Override
    public HashMap<TLogParser.Event, File> match(List<TLogParser.Event> events, ArrayList<File> photos) {
        if(events.isEmpty() || photos.isEmpty())
            return null;

        TreeMap<Long, Event> sortedEvents = new TreeMap<>();
        TreeMap<Long, File> filteredPhotos = new TreeMap<>();

        //Find the msg_named_value_int event
        Event startEvent = null;

        for(Event event : events){
            MAVLinkMessage eventMsg = event.getMavLinkMessage();

            //Only store the msg_camera_feedback events.
            if(eventMsg instanceof msg_camera_feedback) {
                sortedEvents.put(event.getTimestamp(), event);
            }
            else if(eventMsg instanceof msg_named_value_int){
                if(startEvent == null){
                    startEvent = event;
                }
                else{
                    Log.w(TAG, "There's more than one msg_named_value_int event in the event list.");
                    //Defaulting to the event with the earliest timestamp
                    if(startEvent.getTimestamp() > event.getTimestamp()) {
                        startEvent = event;
                    }
                }
            }
        }

        if(startEvent == null){
            //No start event was found. Aborting the process.
            return null;
        }

        long startTime = startEvent.getTimestamp();
        Log.i(TAG, "Filtering events for mission " + ((msg_named_value_int)startEvent.getMavLinkMessage()).getName() + " with start time " + startTime);

        //Filter the events by timestamp
        SortedMap<Long, Event> filteredEvents = sortedEvents.tailMap(startTime);
        if(filteredEvents.isEmpty()){
            //No events survived the filtering
            return null;
        }

        //Get the end time.
        long endTime = filteredEvents.lastKey();
        if(endTime <= startTime){
            //Invalid time span.
            return null;
        }

        //Get the timezone offset, and apply it to the photo modified time.
        final Calendar calendar = Calendar.getInstance();
        final long timezoneOffset = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)); //Timezone offset in milliseconds

        //Filter and sort the media
        for(File photo: photos){
            //Get the file timestamp
            long modifiedTime = photo.lastModified();
            long updatedTime = modifiedTime + timezoneOffset;

            if(startTime <= updatedTime && updatedTime <= endTime){
                filteredPhotos.put(modifiedTime, photo);
            }
        }

        if(filteredPhotos.isEmpty()){
            //No matching media
            return null;
        }

        HashMap<Event, File> result = new HashMap<>();

        ArrayList<Event> eventCollection = new ArrayList<>(sortedEvents.values());
        int eventSize = eventCollection.size();

        ArrayList<File> photoCollection = new ArrayList<>(filteredPhotos.values());
        int photoSize = photoCollection.size();

        for(int e = 0, p = 0; e < eventSize && p < photoSize; e++, p++){
            result.put(eventCollection.get(e), photoCollection.get(p));
        }

        return result;
    }
}
