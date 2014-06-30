package org.droidplanner.android;

import org.droidplanner.android.gcs.FollowMe;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.core.bus.events.DroneConnectedEvent;
import org.droidplanner.core.bus.events.DroneDisconnectedEvent;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.communication.service.MAVLinkClient;
import org.droidplanner.android.communication.service.NetworkStateReceiver;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.utils.DroidplannerPrefs;
import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.bus.events.DroneEvent;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;

import android.content.Context;
import android.os.SystemClock;

import com.MAVLink.Messages.MAVLinkMessage;

import de.greenrobot.event.EventBus;

public class DroidPlannerApp extends ErrorReportApp implements MAVLinkStreams.MavlinkInputStream,
        DroneInterfaces.OnDroneListener {

	public Drone drone;
    public FollowMe followMe;
    public MissionProxy missionProxy;
	private MavLinkMsgHandler mavLinkMsgHandler;

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

		DroidplannerPrefs pref = new DroidplannerPrefs(context);
		drone = new Drone(MAVClient, clock, handler, pref);
		drone.events.addDroneListener(this);

        missionProxy = new MissionProxy(drone.mission);
		mavLinkMsgHandler = new org.droidplanner.core.MAVLink.MavLinkMsgHandler(drone);

        followMe = new FollowMe(this, drone);
        NetworkStateReceiver.register(context);

        GAUtils.initGATracker(this);
        GAUtils.startNewSession(context);
	}

	@Override
	public void notifyReceivedData(MAVLinkMessage msg) {
		mavLinkMsgHandler.receiveData(msg);
	}

	@Override
	public void notifyConnected() {
		drone.events.notifyDroneEvent(DroneEventsType.CONNECTED);

        //Broadcast the events
        final EventBus bus = EventBus.getDefault();
        bus.removeStickyEvent(DroneDisconnectedEvent.class);
        bus.postSticky(new DroneConnectedEvent());

	}

	@Override
	public void notifyDisconnected() {
        drone.events.notifyDroneEvent(DroneEventsType.DISCONNECTED);

        //Broadcast the events
        final EventBus bus = EventBus.getDefault();

        //Remove all prior drone event broadcasts.
        bus.removeStickyEvent(DroneEvent.class);
        bus.postSticky(new DroneDisconnectedEvent());
    }

    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        mNotificationHandler.onDroneEvent(event, drone);

        switch (event) {
            case MISSION_RECEIVED:
                //Refresh the mission render state
                missionProxy.refresh();
                break;
        }
    }
}
