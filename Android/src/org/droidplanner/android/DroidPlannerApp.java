package org.droidplanner.android;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.droidplanner.android.communication.service.MAVLinkClient;
import org.droidplanner.android.communication.service.NetworkConnectivityReceiver;
import org.droidplanner.android.communication.service.UploaderService;
import org.droidplanner.android.gcs.follow.Follow;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.weather.item.IWeatherItem;
import org.droidplanner.android.weather.provider.IWeatherDataProvider;
import org.droidplanner.android.weather.provider.WeatherDataProvider;
import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;

import android.content.Context;
import android.os.SystemClock;

import com.MAVLink.Messages.MAVLinkMessage;

public class DroidPlannerApp extends ErrorReportApp implements
		MAVLinkStreams.MavlinkInputStream, DroneInterfaces.OnDroneListener, IWeatherDataProvider.AsyncListener {


	private Drone drone;
	public Follow followMe;
	public MissionProxy missionProxy;
	private MavLinkMsgHandler mavLinkMsgHandler;
	private WeatherDataProvider weatherProvider;

	/**
	 * Handles dispatching of status bar, and audible notification.
	 */
	public NotificationHandler mNotificationHandler;

	@Override
	public void onCreate() {
		super.onCreate();

		final Context context = getApplicationContext();
		mNotificationHandler = new NotificationHandler(context);

		MAVLinkClient MAVClient = new MAVLinkClient(this, this);
		Clock clock = new Clock() {
			@Override
			public long elapsedRealtime() {
				return SystemClock.elapsedRealtime();
			}
		};
		Handler handler = new Handler() {
			android.os.Handler handler = new android.os.Handler();

			@Override
			public void removeCallbacks(Runnable thread) {
				handler.removeCallbacks(thread);
			}

			@Override
			public void postDelayed(Runnable thread, long timeout) {
				handler.postDelayed(thread, timeout);
			}
		};

		DroidPlannerPrefs pref = new DroidPlannerPrefs(context);
		drone = new Drone(MAVClient, clock, handler, pref);
		getDrone().events.addDroneListener(this);

		missionProxy = new MissionProxy(getDrone().mission);
		mavLinkMsgHandler = new org.droidplanner.core.MAVLink.MavLinkMsgHandler(getDrone());

		followMe = new Follow(this, getDrone(), handler);
				
		weatherProvider = new WeatherDataProvider(this);
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(weatherUpdateTask, 0, 1, TimeUnit.HOURS);

		GAUtils.initGATracker(this);
		GAUtils.startNewSession(context);

        // Any time the application is started, do a quick scan to see if we need any uploads
        startService(UploaderService.createIntent(this));
	}
	
	private Runnable weatherUpdateTask = new Runnable() {
		
		@Override
		public void run() {
			weatherProvider.getWind(drone.GPS.getPosition());
			weatherProvider.getSolarRadiation();
			
		}
	};

	@Override
	public void notifyReceivedData(MAVLinkMessage msg) {
		mavLinkMsgHandler.receiveData(msg);
	}

	@Override
	public void notifyConnected() {
		getDrone().events.notifyDroneEvent(DroneEventsType.CONNECTED);
	}

	@Override
	public void notifyDisconnected() {
		getDrone().events.notifyDroneEvent(DroneEventsType.DISCONNECTED);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		mNotificationHandler.onDroneEvent(event, drone);

		switch (event) {
		case MISSION_RECEIVED:
			// Refresh the mission render state
			missionProxy.refresh();
			break;
		default:
			break;
		}
	}

	public Drone getDrone() {
		return drone;
	}

	@Override
	public void onWeatherFetchSuccess(IWeatherItem item) {
		if (drone.MavClient.isConnected()){ 
			mNotificationHandler.onWeatherFetchSuccess(item);
		}
		
	}

	

	
}
