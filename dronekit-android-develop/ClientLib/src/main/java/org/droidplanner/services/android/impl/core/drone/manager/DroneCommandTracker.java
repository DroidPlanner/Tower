package org.droidplanner.services.android.impl.core.drone.manager;

import android.os.Handler;
import android.os.RemoteException;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_command_ack;
import com.MAVLink.common.msg_command_long;
import com.MAVLink.common.msg_set_mode;

import com.o3dr.services.android.lib.model.ICommandListener;

import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

/**
 * Handles tracking and dispatching of the command listener events.
 * Created by Fredia Huya-Kouadio on 6/24/15.
 */
public class DroneCommandTracker {

    private static final long COMMAND_TIMEOUT_PERIOD = 2000l; //2 seconds

    private final Handler handler;

    private final ConcurrentHashMap<Integer, CallbackKey> keyStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<CallbackKey, AckCallback> callbackStore = new ConcurrentHashMap<>();

    DroneCommandTracker(Handler handler) {
        this.handler = handler;
    }

    public void onCommandSubmitted(MAVLinkMessage command, ICommandListener listener) {
        if (command == null || listener == null)
            return;

        if (command instanceof msg_command_long) {
            onCommandSubmittedImpl((msg_command_long) command, listener);
        } else if (command instanceof msg_set_mode) {
            onCommandSubmittedImpl((msg_set_mode) command, listener);
        }
    }

    private void onCommandSubmittedImpl(msg_command_long command, ICommandListener listener) {
        final int commandId = command.command;
        final CallbackKey<msg_command_ack> key = new CallbackKey<msg_command_ack>(commandId) {

            @Override
            public int checkAckResult(msg_command_ack result) {
                return result.result;
            }
        };
        final AckCallback callback = new AckCallback(listener, commandId);

        keyStore.put(commandId, key);
        callbackStore.put(key, callback);

        handler.postDelayed(callback, COMMAND_TIMEOUT_PERIOD);
    }

    private void onCommandSubmittedImpl(final msg_set_mode command, ICommandListener listener) {
        final int commandId = command.msgid;

        final CallbackKey<msg_command_ack> key = new CallbackKey<msg_command_ack>(commandId) {
            @Override
            public int checkAckResult(msg_command_ack result) {
                return result.result;
            }
        };
        final AckCallback callback = new AckCallback(listener, commandId);

        keyStore.put(commandId, key);
        callbackStore.put(key, callback);

        handler.postDelayed(callback, COMMAND_TIMEOUT_PERIOD);
    }

    public void onCommandAck(int commandId, Object ack) {
        switch (commandId) {
            case msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK:
                onCommandAckImpl((msg_command_ack) ack);
                break;
        }
    }

    private void onCommandAckImpl(msg_command_ack ack) {
        final CallbackKey<msg_command_ack> key = keyStore.get(ack.command);
        if (key == null)
            return;

        final AckCallback callback = callbackStore.remove(key);
        if (callback != null) {
            handler.removeCallbacks(callback);
            callback.setAckResult(key.checkAckResult(ack));
            handler.post(callback);
        }
    }

    private static abstract class CallbackKey<T> {
        private final int commandId;

        CallbackKey(int commandId) {
            this.commandId = commandId;
        }

        public abstract int checkAckResult(T result);

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;

            if (!(o instanceof CallbackKey))
                return false;

            CallbackKey that = (CallbackKey) o;
            return this.commandId == that.commandId;
        }

        @Override
        public int hashCode() {
            return this.commandId;
        }
    }

    private class AckCallback implements Runnable {

        private static final int COMMAND_TIMED_OUT = -1;
        private static final int COMMAND_SUCCEED = 0;

        private int ackResult = COMMAND_TIMED_OUT;

        private final ICommandListener listener;
        private final int ackId;

        AckCallback(ICommandListener listener, int ackId) {
            this.listener = listener;
            this.ackId = ackId;
        }

        void setAckResult(int result) {
            this.ackResult = result;
        }

        @Override
        public void run() {
            if (listener == null)
                return;

            final CallbackKey key = keyStore.remove(ackId);
            if (key != null)
                callbackStore.remove(key);

            Timber.d("Callback with ack result %d", ackResult);

            try {
                switch (ackResult) {
                    case COMMAND_TIMED_OUT:
                        listener.onTimeout();
                        break;

                    case COMMAND_SUCCEED:
                        listener.onSuccess();
                        break;

                    default:
                        listener.onError(ackResult);
                        break;
                }
            } catch (RemoteException e) {
                Timber.e(e, e.getMessage());
            }
        }
    }
}
