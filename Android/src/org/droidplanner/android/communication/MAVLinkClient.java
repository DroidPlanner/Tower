package org.droidplanner.android.communication;

import org.droidplanner.R;
import org.droidplanner.android.communication.service.MAVLinkService;
import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionListener;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;

/**
 * Provide a common class for some ease of use functionality
  */
public class MAVLinkClient implements MAVLinkStreams.MAVLinkOutputStream {

	private static final String TAG = MAVLinkClient.class.getSimpleName();

    /**
     * Used to post updates to the main thread.
     */
    private final Handler mHandler = new Handler();

    private final MavLinkConnectionListener mConnectionListener = new MavLinkConnectionListener() {
        private final Runnable mConnectedNotification = new Runnable() {
            @Override
            public void run() {
                listener.notifyConnected();
            }
        };

        private final Runnable mDisconnectedNotification = new Runnable() {
            @Override
            public void run() {
                listener.notifyDisconnected();
                closeConnection();
            }
        };

        @Override
        public void onConnect() {
            mHandler.post(mConnectedNotification);
        }

        @Override
        public void onReceiveMessage(final MAVLinkMessage msg) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.notifyReceivedData(msg);
                }
            });
        }

        @Override
        public void onDisconnect() {
            mHandler.post(mDisconnectedNotification);
        }

        @Override
        public void onComError(final String errMsg) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(parent, mMavLinkErrorPrefix + " " + errMsg, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    /**
     *  Defines callbacks for service binding, passed to bindService()
     *  */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = (MAVLinkService.MavLinkServiceApi)service;
            onConnectedService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            onDisconnectService();
        }
    };

	private final Context parent;
	private final MAVLinkStreams.MavlinkInputStream listener;
    private final String mMavLinkErrorPrefix;

    private MAVLinkService.MavLinkServiceApi mService;
	private boolean mIsBound;

	public MAVLinkClient(Context context, MAVLinkStreams.MavlinkInputStream listener) {
		parent = context;
		this.listener = listener;
        mMavLinkErrorPrefix = context.getString(R.string.MAVLinkError);
	}

	private void openConnection() {
        if(mIsBound) {
            connectMavLink();
        }
        else{
            parent.bindService(new Intent(parent, MAVLinkService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
        }
	}

	private void closeConnection() {
		if (mIsBound) {
            if(mService.getConnectionStatus() == MavLinkConnection.MAVLINK_CONNECTED){
                Toast.makeText(parent, R.string.status_disconnecting, Toast.LENGTH_SHORT).show();
                mService.disconnectMavLink();
            }

            mService.removeMavLinkConnectionListener(TAG);

            // Unbinding the service.
            parent.unbindService(mConnection);
            onDisconnectService();
		}
	}

	@Override
	public void sendMavPacket(MAVLinkPacket pack) {
		if (!isConnected()) {
			return;
		}

        mService.sendData(pack);
	}

    private void connectMavLink(){
        Toast.makeText(parent, R.string.status_connecting, Toast.LENGTH_SHORT).show();
        mService.connectMavLink();
        mService.addMavLinkConnectionListener(TAG, mConnectionListener);
    }

	private void onConnectedService() {
        mIsBound = true;
        connectMavLink();
	}

	private void onDisconnectService() {
		mIsBound = false;
		listener.notifyDisconnected();
	}

	@Override
	public void queryConnectionState() {
		if (isConnected()) {
			listener.notifyConnected();
		} else {
			listener.notifyDisconnected();
		}
	}

	@Override
	public boolean isConnected() {
		return mIsBound && mService.getConnectionStatus() == MavLinkConnection.MAVLINK_CONNECTED;
	}

	@Override
	public void toggleConnectionState() {
		if (isConnected()) {
			closeConnection();
		} else {
			openConnection();
		}
	}
}
