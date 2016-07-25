package org.droidplanner.android.utils;

import android.content.Context;
import android.net.Uri;

import com.o3dr.android.client.utils.FileUtils;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;

import java.io.File;

/**
 * Created by fredia on 6/11/16.
 */
public class TLogUtils {

    private static final String DIRECTORY_TLOGS = "tlogs";
    private static final String TLOG_FILENAME_EXT = ".tlog";
    private static final String TLOG_PREFIX = "log";

    // Private to prevent instantiation
    private TLogUtils(){}

    /**
     * Return the directory where the generated tlogs logging files are stored.
     * @return File to the tlogs directory
     */
    private static File getTLogsDirectory(Context context){
        File tlogDir = new File(context.getExternalFilesDir(null), DIRECTORY_TLOGS);
        if(!tlogDir.isDirectory()){
            tlogDir.mkdirs();
        }

        return tlogDir;
    }

    /**
     * Generate a tlog filename based on the given parameters
     * @param connectionTypeLabel Label describing the connection type (i.e: usb, tcp,...)
     * @param connectionTimestamp Timestamp when the connection was established
     * @return Filename for a tlog file
     */
    private static String getTLogFilename(String connectionTypeLabel, long connectionTimestamp){
        return TLOG_PREFIX
                + "_" + connectionTypeLabel
                + "_" + FileUtils.getTimeStamp(connectionTimestamp)
                + TLOG_FILENAME_EXT;
    }

    /**
     * Returns the uri where the tlog data should be logged.
     * @param context
     * @param connectionType
     * @param connectionTimestamp
     * @return
     */
    public static Uri getTLogLoggingUri(Context context, @ConnectionType.Type int connectionType, long connectionTimestamp){
        File tlogLoggingFile = new File(getTLogsDirectory(context),
            getTLogFilename(ConnectionType.getConnectionTypeLabel(connectionType), connectionTimestamp));
        return Uri.fromFile(tlogLoggingFile);
    }
}
