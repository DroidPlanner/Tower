package org.droidplanner.android.dialogs.openfile;

import org.droidplanner.android.utils.file.IO.TLogReader;

import com.MAVLink.Messages.ardupilotmega.msg_global_position_int;

public abstract class OpenTLogDialog extends OpenFileDialog {
    public abstract void tlogFileLoaded(TLogReader reader);

    @Override
    protected FileReader createReader() {
        return new TLogReader(msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT);
    }

    @Override
    protected void onDataLoaded(FileReader reader) {
        tlogFileLoaded((TLogReader) reader);
    }
}