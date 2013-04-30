package com.droidplanner.service;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.droidplanner.FlightDataActivity;
import com.droidplanner.R;
import com.droidplanner.service.MAVLinkConnection.MavLinkConnectionListner;

/**
 * http://developer.android.com/guide/components/bound-services.html#Messenger
 * 
 */
public class MAVLinkService extends Service implements MavLinkConnectionListner{

	public static final int MSG_GET_CONNECTION_STATE = 0;
	public static final int MSG_SAY_HELLO = 1;
	public static final int MSG_RECEIVED_DATA = 2;
	public static final int MSG_DEVICE_DISCONNECTED = 3;
	public static final int MSG_DEVICE_CONNECTED = 4;
	public static final int MSG_REGISTER_CLIENT = 5;
	public static final int MSG_UNREGISTER_CLIENT = 6;
	public static final int MSG_TOGGLE_CONNECTION_STATE = 7;
	public static final int MSG_SEND_DATA = 8;

	private WakeLock wakeLock;
	private MAVLinkConnection mavConnection;
	// Messaging
	Messenger msgCenter = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());

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
				Log.d("Service", "Register Client");
				msgCenter = msg.replyTo;
				// This fall-trough is intentional since when registering a new
				// client we will want to know the state of the MAVLink
				// connection
			case MSG_GET_CONNECTION_STATE:
				Log.d("Service", "What is the connection state?");
				int state = (mavConnection!=null) ? MSG_DEVICE_CONNECTED
						: MSG_DEVICE_DISCONNECTED;
				try {
					Message msg_state = Message.obtain(null, state);
					msgCenter.send(msg_state);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case MSG_UNREGISTER_CLIENT:
				Log.d("Service", "Unregister Client");
				msgCenter = null;
				break;

			case MSG_TOGGLE_CONNECTION_STATE:
				Log.d("Service", "Toglle connection to Device");
				toggleConnectionState();
				break;

			case MSG_SEND_DATA:
				Bundle b = msg.getData();
				MAVLinkPacket packet = (MAVLinkPacket) b.getSerializable("msg");
				if (mavConnection!=null) {					
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
				Message msg = Message.obtain(null, MSG_RECEIVED_DATA);
				Bundle data = new Bundle();
				data.putSerializable("msg", m);
				msg.setData(data);
				msgCenter.send(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();

		}
	}

	private void sendMessage(int m) {
		try {
			if (msgCenter != null) {
				Message msg = Message.obtain(null, m);
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
		sendMessage(MSG_DEVICE_DISCONNECTED);
		releaseWakelock();
		updateNotification(getResources().getString(R.string.disconnected));
	}

	@Override
	public void onConnect() {
		aquireWakelock();
		sendMessage(MSG_DEVICE_CONNECTED);
		updateNotification(getResources().getString(R.string.conected));
	}

	@Override
	public void onCreate() {
		super.onCreate();
		showNotification();
	}

	@SuppressWarnings("deprecation")
	protected void aquireWakelock() {
		if(wakeLock==null){
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "CPU");	// Use PARTIAL_WAKE_LOCK, and another pref to keep the screen on
			wakeLock.acquire();
		}
	}
	
	protected void releaseWakelock() {
		if(wakeLock!=null){
			wakeLock.release();		
			wakeLock = null;
		}
	}

	@Override
	public void onDestroy() {
		if(mavConnection !=null){
			mavConnection.disconnect();
			mavConnection = null;
		}
		dismissNotification();
		super.onDestroy();
	}

	/**
	 * Toggle the current state of the MAVlink connection. Starting and closing
	 * the as needed. May throw a onConnect or onDisconnect callback
	 */
	public void toggleConnectionState() {
		if (mavConnection != null) {
			mavConnection.disconnect();
			mavConnection = null;
		} else {
			String connectionType = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("pref_connection_type", "");
			if (connectionType.equals("USB")) {
				mavConnection = new UsbConnection(this);
			} else if (connectionType.equals("TCP")) {
				mavConnection = new TcpConnection(this);
			} else {
				return;
			}
			mavConnection.start();
		}
	}
	
	/**
	 * Show a notification while this service is running.
	 */
	static final int StatusBarNotification = 1;

	private void showNotification() {
		updateNotification(getResources().getString(R.string.disconnected));
	}

	private void updateNotification(String text) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getResources().getString(R.string.app_title))
				.setContentText(text);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, FlightDataActivity.class), 0);
		mBuilder.setContentIntent(contentIntent);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(StatusBarNotification, mBuilder.build());
	}

	private void dismissNotification() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancelAll();

	}
}
