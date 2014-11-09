package org.droidplanner.android.utils.file.IO;

import android.os.RemoteException;
import android.util.Log;

import com.ox3dr.services.android.lib.drone.mission.item.raw.GlobalPositionIntMessage;
import com.ox3dr.services.android.lib.model.ITLogApi;

import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.utils.file.DirectoryPath;
import org.droidplanner.android.utils.file.FileList;
import org.droidplanner.android.utils.file.FileManager;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Read tlog file w/ optional message filter
 *
 * <timestamp><MavLink packet>...
 *
 * See http://qgroundcontrol.org/mavlink for details
 *
 */
public class TLogReader implements OpenFileDialog.FileReader {

    private static final String TAG = TLogReader.class.getSimpleName();
    public static final int MSGFILTER_NONE = -1;

    public static class Event
    {
        private GlobalPositionIntMessage mavLinkMessage;

        public Event(GlobalPositionIntMessage mavLinkMessage) {
            this.mavLinkMessage = mavLinkMessage;
        }

        public GlobalPositionIntMessage getMavLinkMessage() {
            return mavLinkMessage;
        }
    }

    private static final int TIMESTAMP_SIZE = Long.SIZE / Byte.SIZE;

    private final ITLogApi tlogApi;
    private final int msgFilter;
    private final List<Event> logEvents = new LinkedList<Event>();


    public TLogReader(ITLogApi tlogApi, int msgFilter) {
        this.tlogApi = tlogApi;
        this.msgFilter = msgFilter;
    }

    public List<Event> getLogEvents() {
        return logEvents;
    }

    private boolean openTLog(String file) {
        if (!FileManager.isExternalStorageAvailable()) {
            return false;
        }

        try {
            GlobalPositionIntMessage[] positionMsgs = tlogApi.loadGlobalPositionIntMessages(file);
            for(GlobalPositionIntMessage msg : positionMsgs){
                logEvents.add(new Event(msg));
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getPath() {
        return DirectoryPath.getTLogPath().getPath() + "/";
    }

    @Override
    public String[] getFileList() {
        return FileList.getTLogFileList();
    }

    @Override
    public boolean openFile(String file) {
        return openTLog(file);
    }
}