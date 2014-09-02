package org.droidplanner.android.communication.connection;

import java.io.File;

import android.content.Context;
import android.preference.PreferenceManager;

import org.droidplanner.android.utils.AndroidLogger;
import org.droidplanner.android.utils.file.FileStream;
import org.droidplanner.core.MAVLink.connection.UdpConnection;
import org.droidplanner.core.model.Logger;

public class AndroidUdpConnection extends UdpConnection {

    private final Context mContext;

	public AndroidUdpConnection(Context context) {
		mContext = context;
	}

    @Override
    protected int loadServerPort(){
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(mContext).getString
                ("pref_udp_server_port", "14550"));
    }

    @Override
    protected Logger initLogger() {
        return AndroidLogger.getLogger();
    }

    @Override
    protected File getTempTLogFile() {
        return FileStream.getTLogFile();
    }

    @Override
    protected void commitTempTLogFile(File tlogFile) {
        FileStream.commitFile(tlogFile);
    }

}
