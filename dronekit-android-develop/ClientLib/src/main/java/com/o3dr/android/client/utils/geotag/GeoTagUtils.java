package com.o3dr.android.client.utils.geotag;

import android.media.ExifInterface;

import com.MAVLink.ardupilotmega.msg_camera_feedback;
import com.o3dr.android.client.utils.data.tlog.TLogParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Fredia Huya-Kouadio on 1/12/16.
 */
public class GeoTagUtils {

    //Prevent instantiation
    private GeoTagUtils(){}

    public interface GeoTagListener {
        void onProgress(int numProcessed, int numTotal);
    }

    /**
     *
     * @param saveDir
     * @param events
     * @param photos
     * @param geoTagAlg
     * @param listener
     * @return
     */
    public static ResultObject geotag(File saveDir, List<TLogParser.Event> events, ArrayList<File> photos, GeoTagAsyncTask.GeoTagAlgorithm geoTagAlg, GeoTagListener listener){
        ResultObject resultObject = new ResultObject();

        try {
            HashMap<TLogParser.Event, File> eventsPhotos = new HashMap<>();
            HashMap<File, File> geoTaggedFiles = new HashMap<>();
            HashMap<File, Exception> failedFiles = new HashMap<>();
            resultObject.setResult(eventsPhotos, geoTaggedFiles, failedFiles);

            if (!saveDir.mkdirs()) {
                resultObject.setException(new IllegalStateException("Failed to create directory for images"));
                return resultObject;
            }

            HashMap<TLogParser.Event, File> matchedPhotos = geoTagAlg.match(events, photos);
            if(matchedPhotos == null || matchedPhotos.isEmpty()){
                resultObject.setException(new IllegalStateException("Unable to match the media set for geotagging."));
                return resultObject;
            }

            if (!hasEnoughMemory(saveDir, matchedPhotos.values())) {
                resultObject.setException(new IllegalStateException("Insufficient external storage space."));
                return resultObject;
            }

            int numTotal = matchedPhotos.size();
            int numProcessed = 0;
            for (Map.Entry<TLogParser.Event, File> entry : matchedPhotos.entrySet()) {
                File photo = entry.getValue();

                TLogParser.Event event = entry.getKey();
                File newFile = new File(saveDir, photo.getName());
                try {
                    copyFile(photo, newFile);
                    updateExif(event, newFile);

                    eventsPhotos.put(event, newFile);
                    geoTaggedFiles.put(photo, newFile);
                } catch (Exception e) {
                    failedFiles.put(photo, e);
                }

                numProcessed++;
                if(listener != null){
                    listener.onProgress(numProcessed, numTotal);
                }
            }

        } catch (Exception e) {
            resultObject.setException(e);
        }

        return resultObject;
    }

    private static boolean hasEnoughMemory(File file, Collection<File> photos) {
        long freeBytes = file.getUsableSpace();
        long bytesNeeded = 0;
        for (File photo : photos) {
            bytesNeeded += photo.length();
        }

        if (bytesNeeded > freeBytes) {
            return false;
        }
        return true;
    }

    private static void copyFile(File inputPath, File outputPath) throws IOException {
        InputStream in = new FileInputStream(inputPath);
        OutputStream out = new FileOutputStream(outputPath);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();

        // write the output file (You have now copied the file)
        out.flush();
        out.close();
    }

    private static void updateExif(TLogParser.Event event, File photoFile) throws IOException {
        msg_camera_feedback msg = ((msg_camera_feedback) event.getMavLinkMessage());
        double lat = (double) msg.lat / 10000000;
        double lng = (double) msg.lng / 10000000;
        String alt = String.valueOf(msg.alt_msl);

        ExifInterface exifInterface = new ExifInterface(photoFile.getPath());
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertLatLngToDMS(lng));
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertLatLngToDMS(lat));
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, lat < 0 ? "S" : "N");
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, lng < 0 ? "W" : "E");
        exifInterface.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, alt);
        exifInterface.saveAttributes();
    }

    private static String convertLatLngToDMS(double coord) {
        double dDegree = Math.abs(coord);
        int degree = (int) dDegree;

        double dMinute = (dDegree - degree) * 60;
        int minute = (int) dMinute;

        double dSecond = (dMinute - minute) * 60;
        int second = (int) (dSecond * 1000);

        return String.format("%s/1,%s/1,%s/1000", degree, minute, second);
    }

    public static class ResultObject {
        private boolean didSucceed;
        private HashMap<TLogParser.Event, File> eventsPhotos;
        private HashMap<File, File> geoTaggedPhotos;
        private HashMap<File, Exception> failedFiles;
        private Exception exception;

        public boolean didSucceed() {
            return didSucceed;
        }

        public void setResult(HashMap<TLogParser.Event, File> eventsPhotos, HashMap<File, File> geoTaggedPhotos, HashMap<File, Exception> failedFiles) {
            didSucceed = true;
            this.eventsPhotos = eventsPhotos;
            this.geoTaggedPhotos = geoTaggedPhotos;
            this.failedFiles = failedFiles;
        }

        public HashMap<File, File> getGeoTaggedPhotos() {
            return geoTaggedPhotos;
        }

        public HashMap<TLogParser.Event, File> getEventsPhotos() {
            return eventsPhotos;
        }

        public HashMap<File, Exception> getFailedFiles() {
            return failedFiles;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            didSucceed = false;
            this.exception = exception;
        }
    }
}
