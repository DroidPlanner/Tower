package org.droidplanner.services.android.impl.core.drone;

import android.os.Handler;
import android.os.RemoteException;

import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import com.o3dr.services.android.lib.model.ICommandListener;

import timber.log.Timber;

public class DroneVariable<T extends MavLinkDrone> {

	static int UNSIGNED_BYTE_MIN_VALUE = 0;
	static int UNSIGNED_BYTE_MAX_VALUE = 255;

	protected T myDrone;

	public DroneVariable(T myDrone) {
		this.myDrone = myDrone;
	}

	/**
	 * Convenience method to post a success event to the listener.
	 * @param handler Use to dispatch the event
	 * @param listener To whom the event is dispatched.
	 */
	protected void postSuccessEvent(Handler handler, final ICommandListener listener){
		if(handler != null && listener != null){
			handler.post(new Runnable() {
				@Override
				public void run() {
					try {
						listener.onSuccess();
					} catch (RemoteException e) {
						Timber.e(e, e.getMessage());
					}
				}
			});
		}
	}

	/**
	 * Convenience method to post an error event to the listener.
	 * @param handler Use to dispatch the event
	 * @param listener To whom the event is dispatched.
	 *                 @param error Execution error.
	 */
	protected void postErrorEvent(Handler handler, final ICommandListener listener, final int error){
		if(handler != null && listener != null){
			handler.post(new Runnable() {
				@Override
				public void run() {
					try {
						listener.onError(error);
					} catch (RemoteException e) {
						Timber.e(e, e.getMessage());
					}
				}
			});
		}
	}

	/**
	 * Convenience method to post a timeout event to the listener.
	 * @param handler Use to dispatch the event
	 * @param listener To whom the event is dispatched.
	 */
	protected void postTimeoutEvent(Handler handler, final ICommandListener listener){
		if(handler != null && listener != null){
			handler.post(new Runnable() {
				@Override
				public void run() {
					try {
						listener.onTimeout();
					} catch (RemoteException e) {
						Timber.e(e, e.getMessage());
					}
				}
			});
		}
	}

	public static short validateToUnsignedByteRange(int id){
		if(id < UNSIGNED_BYTE_MIN_VALUE || id > UNSIGNED_BYTE_MAX_VALUE){
			throw new IllegalArgumentException("Value is outside of the range of an sysid/compid byte: " + id);
		}
		return (short)id;
	}


}