package org.droidplanner.service;

import java.util.Timer;
import java.util.TimerTask;

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

    /**
     * This is used as tag for logging.
     */
    private static final String TAG = MAVLinkClient.class.getSimpleName();

	public static final int MSG_RECEIVED_DATA = 0;
	public static final int MSG_SELF_DESTRY_SERVICE = 1;
	public static final int MSG_TIMEOUT = 2;

	Context parent;
	private OnMavlinkClientListner listner;
	Messenger mService = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	private boolean mIsBound;
	private Timer timeOutTimer;
	private int timeOutCount;
	private long timeOut;
	private int timeOutRetry;

	public interface OnMavlinkClientListner {
		public void notifyConnected();

		public void notifyDisconnected();

		public void notifyReceivedData(MAVLinkMessage m);

		void notifyTimeOut(int timeOutCount);
	}

	public MAVLinkClient(Context context, OnMavlinkClientListner listner) {
		parent = context;
		this.listner = listner;
	}

	public void init() {
		parent.bindService(new Intent(parent, MAVLinkService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	public void close() {
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

	public void setTimeOutValue(long timeout_ms) {
		this.timeOut = timeout_ms;
	}

	public long getTimeOutValue() {
		if (this.timeOut <= 0)
			return 3000; // default value

		return this.timeOut;
	}

	public void setTimeOutRetry(int timeout_retry) {
		this.timeOutRetry = timeout_retry;
	}

	public int getTimeOutRetry() {
		if (this.timeOutRetry <= 0)
			return 3; // default value

		return this.timeOutRetry;
	}

	public synchronized void resetTimeOut() {
		if (timeOutTimer != null) {
			timeOutTimer.cancel();
			timeOutTimer = null;
			/*
			 * Log.d("TIMEOUT", "reset " + String.valueOf(timeOutTimer));
			 */
		}
	}

	public void setTimeOut() {
		setTimeOut(this.timeOut, true);
	}

	public void setTimeOut(boolean resetTimeOutCount) {
		setTimeOut(this.timeOut, resetTimeOutCount);
	}

	public synchronized void setTimeOut(long timeout_ms,
			boolean resetTimeOutCount) {
		/*
		 * Log.d("TIMEOUT", "set " + String.valueOf(timeout_ms));
		 */
		resetTimeOut();
		if (resetTimeOutCount)
			timeOutCount = 0;

		if (timeOutTimer == null) {
			timeOutTimer = new Timer();
			timeOutTimer.schedule(new TimerTask() {
				public void run() {
					if (timeOutTimer != null) {
						resetTimeOut();
						timeOutCount++;

						/*
						 * Log.d("TIMEOUT", "timed out");
						 */

						listner.notifyTimeOut(timeOutCount);
					}
				}
			}, timeout_ms); // delay in milliseconds
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
				listner.notifyReceivedData(m);
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
        if(mService == null){
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
		listner.notifyConnected();
	}

	private void onDisconnectService() {
		mIsBound = false;
		listner.notifyDisconnected();
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

	public void toggleConnectionState() {
		if (isConnected()) {
			close();
		} else {
			init();
		}
	}
}
