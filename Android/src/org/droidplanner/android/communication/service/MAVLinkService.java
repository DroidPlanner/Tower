package org.droidplanner.android.communication.service;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.communication.connection.MAVLinkConnection;
import org.droidplanner.android.communication.connection.MAVLinkConnection.MavLinkConnectionListener;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.notifications.StatusBarNotificationProvider;
import org.droidplanner.android.utils.Constants;
import org.droidplanner.android.utils.Utils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;

/**
 * http://developer.android.com/guide/components/bound-services.html#Messenger
 * 
 */

public class MAVLinkService extends Service implements
		MavLinkConnectionListener {
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SEND_DATA = 3;

	private MAVLinkConnection mavConnection;
	Messenger msgCenter = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	private boolean couldNotOpenConnection = false;

	/**
	 * 
	 * Handler for Communication Errors Messages used in onComError() to display
	 * Toast msg.
	 * 
	 * */

	private String commErrMsgLocalStore;
	private Handler commErrHandler;

	/**
	 * Handler of incoming messages from clients.
	 */
	@SuppressLint("HandlerLeak")
	// TODO fix this error message
	class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				msgCenter = msg.replyTo;
				if (couldNotOpenConnection) {
					selfDestryService();
				}
				break;
			case MSG_UNREGISTER_CLIENT:
				msgCenter = null;
				break;
			case MSG_SEND_DATA:
				Bundle b = msg.getData();
				MAVLinkPacket packet = (MAVLinkPacket) b.getSerializable("msg");
				if (mavConnection != null) {
					mavConnection.sendMavPacket(packet);
				}
			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	private void notifyNewMessage(MAVLinkMessage m) {
		try {
			if (msgCenter != null) {
				Message msg = Message.obtain(null,
						MAVLinkClient.MSG_RECEIVED_DATA);
				Bundle data = new Bundle();
				data.putSerializable("msg", m);
				msg.setData(data);
				msgCenter.send(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onReceiveMessage(MAVLinkMessage msg) {
		notifyNewMessage(msg);
	}

	@Override
	public void onDisconnect() {
		couldNotOpenConnection = true;
		selfDestryService();
	}

	private void selfDestryService() {
		try {
			if (msgCenter != null) {
				Message msg = Message.obtain(null,
						MAVLinkClient.MSG_SELF_DESTRY_SERVICE);
				msgCenter.send(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate() {
		super.onCreate();
		commErrMsgLocalStore = getResources().getString(R.string.MAVLinkError);

		commErrHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Toast.makeText(getApplicationContext(), commErrMsgLocalStore,
						Toast.LENGTH_LONG).show();
			}
		};

		connectMAVconnection();

		final StatusBarNotificationProvider statusBarNotification = ((DroidPlannerApp) getApplication()).mNotificationHandler
				.getStatusBarNotificationProvider();

		statusBarNotification.showNotification();
		statusBarNotification.updateNotification(getResources().getString(
				R.string.connected));
	}

	@Override
	public void onDestroy() {
		disconnectMAVConnection();

		final StatusBarNotificationProvider statusBarNotification = ((DroidPlannerApp) getApplication()).mNotificationHandler
				.getStatusBarNotificationProvider();
		statusBarNotification.dismissNotification();

		super.onDestroy();
	}

	/**
	 * Called after the exception raised in any of the MavLinkConnection classes
	 */
	public void onComError(String errMsg) {

		Message errMessageObj = new Message();
		Bundle resBundle = new Bundle();
		resBundle.putString("status", "SUCCESS");
		errMessageObj.obj = resBundle;

		commErrMsgLocalStore = commErrMsgLocalStore + " " + errMsg;
		commErrHandler.sendMessage(errMessageObj);

		Log.d(CONNECTIVITY_SERVICE, commErrMsgLocalStore);
	}

	/**
	 * Toggle the current state of the MAVlink connection. Starting and closing
	 * the as needed. May throw a onConnect or onDisconnect callback
	 */
	private void connectMAVconnection() {
		String connectionType = PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).getString(
				Constants.PREF_CONNECTION_TYPE,
				Constants.DEFAULT_CONNECTION_TYPE);

		Utils.ConnectionType connType = Utils.ConnectionType
				.valueOf(connectionType);
		mavConnection = connType.getConnection(this);
		mavConnection.start();
	}

	private void disconnectMAVConnection() {
		if (mavConnection != null) {
			mavConnection.disconnect();
			mavConnection = null;
		}
	}

}
