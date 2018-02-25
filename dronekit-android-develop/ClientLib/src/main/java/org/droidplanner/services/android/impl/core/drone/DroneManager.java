package org.droidplanner.services.android.impl.core.drone;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.o3dr.services.android.lib.drone.action.ControlActions;
import com.o3dr.services.android.lib.drone.action.GimbalActions;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.services.android.impl.api.DroneApi;
import org.droidplanner.services.android.impl.communication.model.DataLink;
import org.droidplanner.services.android.impl.core.drone.autopilot.Drone;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.ArduSolo;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.SoloComp;
import org.droidplanner.services.android.impl.core.drone.manager.MavLinkDroneManager;
import org.droidplanner.services.android.impl.utils.CommonApiUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridge between the communication channel, the drone instance(s), and the connected client(s).
 */
public class DroneManager<T extends Drone, D> implements DataLink.DataLinkListener<D>, DroneInterfaces.OnDroneListener,
    DroneInterfaces.OnParameterManagerListener, LogMessageListener, DroneInterfaces.AttributeEventListener {

    private static final String TAG = DroneManager.class.getSimpleName();

    public static final String EXTRA_CLIENT_APP_ID = "extra_client_app_id";

    protected final ConcurrentHashMap<String, DroneApi> connectedApps = new ConcurrentHashMap<>();

    protected final Context context;
    protected final Handler handler;

    protected T drone;
    protected final ConnectionParameter connectionParameter;

    public static DroneManager generateDroneManager(Context context, ConnectionParameter connParams, Handler handler) {
        switch (connParams.getConnectionType()) {
            default:
                return new MavLinkDroneManager(context, connParams, handler);
        }
    }

    protected DroneManager(Context context, ConnectionParameter connParams, Handler handler) {
        this.context = context;
        this.handler = handler;
        this.connectionParameter = connParams;
    }

    private void destroyAutopilot() {
        if (drone == null) {
            return;
        }

        drone.destroy();
        drone = null;
    }

    public void destroy() {
        Log.d(TAG, "Destroying drone manager.");

        disconnect();
        destroyAutopilot();

        connectedApps.clear();

    }

    public synchronized void connect(String appId, DroneApi listener, ConnectionParameter connParams) {
        if (listener == null || TextUtils.isEmpty(appId)) {
            return;
        }

        connectedApps.put(appId, listener);
        doConnect(appId, listener, connParams);
    }

    protected void doConnect(String appId, DroneApi listener, ConnectionParameter connParams) {

    }

    private void disconnect() {
        if (!connectedApps.isEmpty()) {
            for (DroneApi client : connectedApps.values()) {
                disconnect(client.getClientInfo());
            }
        }
    }

    /**
     * @return True if we can expect to find a companion computer on the connected channel.
     */
    protected boolean isCompanionComputerEnabled() {
        final int connectionType = connectionParameter.getConnectionType();

        return drone instanceof ArduSolo
            || (connectionType == ConnectionType.TYPE_UDP && SoloComp.isAvailable(context))
            || connectionType == ConnectionType.TYPE_SOLO;

    }

    public int getConnectedAppsCount() {
        return connectedApps.size();
    }

    public void disconnect(DroneApi.ClientInfo clientInfo) {
        String appId = clientInfo.appId;
        if (TextUtils.isEmpty(appId)) {
            return;
        }

        Log.d(TAG, "Disconnecting client " + appId);
        DroneApi listener = connectedApps.remove(appId);

        doDisconnect(appId, listener);
    }

    protected void doDisconnect(String appId, DroneApi listener) {
        if (isConnected() && listener != null) {
            listener.onDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED, drone);
        }

        if (connectedApps.isEmpty()) {
            //Reset the gimbal mount mode
            executeAsyncAction(null, new Action(GimbalActions.ACTION_RESET_GIMBAL_MOUNT_MODE), null);
        }
    }

    protected void notifyDroneEvent(DroneInterfaces.DroneEventsType event) {
        if (drone != null) {
            drone.notifyDroneEvent(event);
        }
    }

    @Override
    public void notifyReceivedData(D data) {

    }

    @Override
    public void onConnectionStatus(LinkConnectionStatus connectionStatus) {
        switch (connectionStatus.getStatusCode()) {
            case LinkConnectionStatus.DISCONNECTED:
                notifyDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED);
                break;

            case LinkConnectionStatus.CONNECTING:
                notifyDroneEvent(DroneInterfaces.DroneEventsType.CONNECTING);
                break;
        }

        if (connectedApps.isEmpty()) {
            return;
        }

        for (DroneApi droneEventsListener : connectedApps.values()) {
            droneEventsListener.onConnectionStatus(connectionStatus);
        }
    }

    public T getDrone() {
        return this.drone;
    }

    public boolean isConnected() {
        return drone != null && drone.isConnected();
    }

    public DroneAttribute getAttribute(DroneApi.ClientInfo clientInfo, String attributeType) {
        switch (attributeType) {
            default:
                return drone == null ? null : drone.getAttribute(attributeType);
        }
    }

    protected boolean executeAsyncAction(Action action, ICommandListener listener) {
        String type = action.getType();

        switch (type) {

            //***************** CONTROL ACTIONS *****************//
            case ControlActions.ACTION_ENABLE_MANUAL_CONTROL:
                if (drone != null) {
                    drone.executeAsyncAction(action, listener);
                } else {
                    CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                }
                return true;

            default:
                if (drone != null) {
                    return drone.executeAsyncAction(action, listener);
                } else {
                    CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                    return true;
                }
        }
    }

    public boolean executeAsyncAction(DroneApi.ClientInfo clientInfo, Action action, ICommandListener listener) {
        String type = action.getType();
        Bundle data = action.getData();

        switch (type) {
            case ControlActions.ACTION_ENABLE_MANUAL_CONTROL:
                data.putString(EXTRA_CLIENT_APP_ID, clientInfo.appId);
                break;
        }
        return executeAsyncAction(action, listener);
    }

    protected void notifyDroneAttributeEvent(String attributeEvent, Bundle eventInfo) {
        if (TextUtils.isEmpty(attributeEvent) || connectedApps.isEmpty()) {
            return;
        }

        for (DroneApi listener : connectedApps.values()) {
            listener.onAttributeEvent(attributeEvent, eventInfo);
        }
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch (event) {
            case HEARTBEAT_FIRST:
            case CONNECTED:
                event = DroneInterfaces.DroneEventsType.CONNECTED;
                break;
        }

        if (connectedApps.isEmpty()) {
            return;
        }

        for (DroneApi droneEventsListener : connectedApps.values()) {
            droneEventsListener.onDroneEvent(event, drone);
        }
    }

    @Override
    public void onBeginReceivingParameters() {
        if (connectedApps.isEmpty()) {
            return;
        }

        for (DroneApi droneEventsListener : connectedApps.values()) {
            droneEventsListener.onBeginReceivingParameters();
        }
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        if (connectedApps.isEmpty()) {
            return;
        }

        for (DroneApi droneEventsListener : connectedApps.values()) {
            droneEventsListener.onParameterReceived(parameter, index, count);
        }
    }

    @Override
    public void onEndReceivingParameters() {
        if (connectedApps.isEmpty()) {
            return;
        }

        for (DroneApi droneEventsListener : connectedApps.values()) {
            droneEventsListener.onEndReceivingParameters();
        }
    }

    public ConnectionParameter getConnectionParameter() {
        return connectionParameter;
    }

    @Override
    public void onMessageLogged(int logLevel, String message) {
        if (connectedApps.isEmpty()) {
            return;
        }

        for (DroneApi listener : connectedApps.values()) {
            listener.onMessageLogged(logLevel, message);
        }
    }

    @Override
    public void onAttributeEvent(String attributeEvent, Bundle eventInfo) {
        notifyDroneAttributeEvent(attributeEvent, eventInfo);
    }
}
