package org.droidplanner.android;

import org.droidplanner.android.communication.service.MAVLinkClient;
import org.droidplanner.android.communication.service.UploaderService;
import org.droidplanner.android.gcs.location.FusedLocation;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.model.Drone;

import android.content.Context;
import android.os.SystemClock;

import com.MAVLink.Messages.MAVLinkMessage;

public class DroidPlannerApp extends ErrorReportApp implements MAVLinkStreams.MavlinkInputStream,
		DroneInterfaces.OnDroneListener {

	private Drone drone;
	private Follow followMe;
	private MissionProxy missionProxy;
	private MavLinkMsgHandler mavLinkMsgHandler;
	private DroidPlannerPrefs prefs;
	/**
	 * Handles dispatching of status bar, and audible notification.
	 */
	public NotificationHandler mNotificationHandler;

	@Override
	public void onCreate() {
		super.onCreate();

		final Context context = getApplicationContext();

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
			public void post(Runnable thread) {
				handler.post(thread);
			}

			@Override
			public void postDelayed(Runnable thread, long timeout) {
				handler.postDelayed(thread, timeout);
			}
		};
		mNotificationHandler = new NotificationHandler(context);

		prefs = new DroidPlannerPrefs(context);
		drone = new DroneImpl(MAVClient, clock, handler, prefs);
		getDrone().addDroneListener(this);

		missionProxy = new MissionProxy(getDrone().getMission());
		mavLinkMsgHandler = new org.droidplanner.core.MAVLink.MavLinkMsgHandler(getDrone());

		followMe = new Follow(getDrone(), handler, new FusedLocation(context));

		GAUtils.initGATracker(this);
		GAUtils.startNewSession(context);

		// Any time the application is started, do a quick scan to see if we
		// need any uploads
		startService(UploaderService.createIntent(this));
	}

	@Override
	public void notifyReceivedData(MAVLinkMessage msg) {
		mavLinkMsgHandler.receiveData(msg);
	}

	@Override
	public void notifyConnected() {
		getDrone().notifyDroneEvent(DroneEventsType.CONNECTED);
	}

	@Override
	public void notifyDisconnected() {
		getDrone().notifyDroneEvent(DroneEventsType.DISCONNECTED);
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

	public DroidPlannerPrefs getPreferences() {
		return prefs;
	}

	public Drone getDrone() {
		return drone;
	}

	public Follow getFollowMe() {
		return followMe;
	}

	public MissionProxy getMissionProxy() {
		return missionProxy;
	}
}
