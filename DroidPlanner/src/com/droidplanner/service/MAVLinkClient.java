package com.droidplanner.service;

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
public class MAVLinkClient {

	Context parent;
	private OnMavlinkClientListner listner;
	Messenger mService = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	private boolean mIsBound;


	public interface OnMavlinkClientListner {
		public void notifyConnected();
		public void notifyDisconnected();
		public void notifyReceivedData(MAVLinkMessage m);
	}

	public MAVLinkClient(Context context, OnMavlinkClientListner listner) {
		parent = context;
		this.listner = listner;
	}

	public void init() {
		Log.d("Client", "Client Init");
		parent.bindService(new Intent(parent, MAVLinkService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	public void close() {
		Log.d("Client", "Client closing");
		if (isConnected()) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							MAVLinkService.MSG_UNREGISTER_CLIENT);
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
			case MAVLinkService.MSG_RECEIVED_DATA:
				Bundle b = msg.getData();
				MAVLinkMessage m = (MAVLinkMessage) b.getSerializable("msg");
				listner.notifyReceivedData(m);
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
		Message msg = Message.obtain(null, MAVLinkService.MSG_SEND_DATA);
		Bundle data = new Bundle();
		data.putSerializable("msg", pack);
		msg.setData(data);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	private void onConnectedService() {
		Log.d("Client", "Client Connected");
		listner.notifyConnected();
	}

	private void onDisconnectService() {
		mIsBound = false;
		listner.notifyDisconnected();
		Log.d("Client", "Client Unbinding");
	}

	public void queryConnectionState() {
		if (mIsBound) {
			listner.notifyConnected();
		} else {
			listner.notifyDisconnected();
		}

	}

	public boolean isConnected() {
		return mIsBound;
	}
}
