package org.droidplanner.android.communication.connection;

import java.io.File;

import android.content.Context;
import android.preference.PreferenceManager;

import org.droidplanner.android.utils.AndroidLogger;
import org.droidplanner.android.utils.file.FileStream;
import org.droidplanner.core.MAVLink.connection.TcpConnection;
import org.droidplanner.core.model.Logger;

public class AndroidTcpConnection extends TcpConnection {

    private final Context mContext;

	public AndroidTcpConnection(Context context) {
        mContext = context;
	}

    @Override
    protected int loadServerPort() {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(mContext).getString
                ("pref_server_port", "0"));
    }

    @Override
    protected String loadServerIP() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getString("pref_server_ip", "");
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
