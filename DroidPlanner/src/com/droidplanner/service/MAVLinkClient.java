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

	/** Messenger for communicating with service. */
	Messenger mService = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	public boolean startedInit = false;

	private OnMavlinkClientListner listner;

	
	
	public interface OnMavlinkClientListner{
		public void notifyConnected();
		public void notifyDisconnected();
		public void notifyReceivedData(MAVLinkMessage m);
	}

	public MAVLinkClient(Context context, OnMavlinkClientListner listner) {
		parent = context;
		this.listner = listner;
	}

	public void onDestroy() {
		if (!startedInit)
			return;
		Log.d("Service", "Client Destroyed");
		try {
			if (mService != null) {
				Message msg = Message.obtain(null,
						MAVLinkService.MSG_UNREGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);

			}
			// Unbinding the service.
			parent.unbindService(mConnection);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e){
			e.printStackTrace();
		}
	}

	public void init() {
		Log.d("Service", "Client Init");
		startedInit = true;
		parent.bindService(
				new Intent(parent, MAVLinkService.class), mConnection,
				Context.BIND_AUTO_CREATE);
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
			case MAVLinkService.MSG_DEVICE_CONNECTED:
				listner.notifyConnected();
				break;

			case MAVLinkService.MSG_DEVICE_DISCONNECTED:
				listner.notifyDisconnected();
				break;

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
				Log.d("Service", "Client Connected");
			} catch (RemoteException e) {
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.d("Service", "Client Disconencted");
			mService = null;
		}
	};

	public void toggleConnectionState() {
		Message msg = Message.obtain(null, MAVLinkService.MSG_TOGGLE_CONNECTION_STATE);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void queryConnectionState() {
		Message msg = Message.obtain(null, MAVLinkService.MSG_GET_CONNECTION_STATE);
		try {
			mService.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
}
