package org.droidplanner.android.communication.service;

import org.droidplanner.core.MAVLink.MAVLinkStreams;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;

// provide a common class for some ease of use functionality
public class MAVLinkClient implements MAVLinkStreams.MAVLinkOutputStream {

	/**
	 * This is used as tag for logging.
	 */
	private static final String TAG = MAVLinkClient.class.getSimpleName();

	public static final int MSG_RECEIVED_DATA = 0;
	public static final int MSG_SELF_DESTRY_SERVICE = 1;
	public static final int MSG_TIMEOUT = 2;

	Context parent;
	private MAVLinkStreams.MavlinkInputStream listener;
	Messenger mService = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	private boolean mIsBound;

	public MAVLinkClient(Context context,
			MAVLinkStreams.MavlinkInputStream listener) {
		parent = context;
		this.listener = listener;
	}

	private void init() {
		parent.bindService(new Intent(parent, MAVLinkService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	private void close() {
		if (isConnected()) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, MAVLinkService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);

				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
				// Unbinding the service.
				parent.unbindService(mConnection);
				onDisconnectService();
			}
		}
	}

	/**
	 * Handler of incoming messages from service.
	 */
	@SuppressLint("HandlerLeak")
	// TODO fix this error message
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// Received data from... somewhere
			case MSG_RECEIVED_DATA:
				Bundle b = msg.getData();
				MAVLinkMessage m = (MAVLinkMessage) b.getSerializable("msg");
				listener.notifyReceivedData(m);
				break;
			case MSG_SELF_DESTRY_SERVICE:
				close();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			try {
				Message msg = Message.obtain(null,
						MAVLinkService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
				onConnectedService();
			} catch (RemoteException e) {
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			onDisconnectService();
		}
	};

	public void sendMavPacket(MAVLinkPacket pack) {
		if (mService == null) {
			return;
		}

		Message msg = Message.obtain(null, MAVLinkService.MSG_SEND_DATA);
		Bundle data = new Bundle();
		data.putSerializable("msg", pack);
		msg.setData(data);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	private void onConnectedService() {
		listener.notifyConnected();
	}

	private void onDisconnectService() {
		mIsBound = false;
		listener.notifyDisconnected();
	}

	public void queryConnectionState() {
		if (mIsBound) {
			listener.notifyConnected();
		} else {
			listener.notifyDisconnected();
		}

	}

	public boolean isConnected() {
		return mIsBound;
	}

	public void toggleConnectionState() {
		if (isConnected()) {
			close();
		} else {
			init();
		}
	}
}
