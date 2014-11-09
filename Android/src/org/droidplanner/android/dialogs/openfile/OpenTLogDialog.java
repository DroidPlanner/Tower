package org.droidplanner.android.dialogs.openfile;

import com.ox3dr.services.android.lib.drone.mission.item.raw.GlobalPositionIntMessage;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.utils.file.IO.TLogReader;

public abstract class OpenTLogDialog extends OpenFileDialog {
    public abstract void tlogFileLoaded(TLogReader reader);

    @Override
    protected FileReader createReader() {
        return new TLogReader(((DroidPlannerApp)this.context).getTlogApi(),
                GlobalPositionIntMessage.MSG_ID);
    }

    @Override
    protected void onDataLoaded(FileReader reader) {
        tlogFileLoaded((TLogReader) reader);
    }
}