package org.droidplanner.core.drone.profiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.droidplanner.core.MAVLink.MavLinkParameters;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_param_value;

/**
 * Class to manage the communication of parameters to the MAV.
 * 
 * Should be initialized with a MAVLink Object, so the manager can send messages
 * via the MAV link. The function processMessage must be called with every new
 * MAV Message.
 * 
 */
public class Parameters extends DroneVariable implements OnDroneListener {

	private static final int TIMEOUT = 1000;

	private int expectedParams;

	private final HashMap<Integer, Parameter> parameters = new HashMap<Integer, Parameter>();

	private DroneInterfaces.OnParameterManagerListener parameterListener;

	public Handler watchdog;
	public Runnable watchdogCallback = new Runnable() {
		@Override
		public void run() {
			onParameterStreamStopped();
		}
	};

	public final ArrayList<Parameter> parameterList = new ArrayList<Parameter>();

	public Parameters(Drone myDrone, Handler handler) {
		super(myDrone);
		this.watchdog = handler;
		myDrone.addDroneListener(this);
	}

	public void refreshParameters() {
		parameters.clear();
        parameterList.clear();

		if (parameterListener != null)
			parameterListener.onBeginReceivingParameters();
		MavLinkParameters.requestParametersList(myDrone);
		resetWatchdog();
	}

    public List<Parameter> getParametersList(){
        return parameterList;
    }

	/**
	 * Try to process a Mavlink message if it is a parameter related message
	 * 
	 * @param msg
	 *            Mavlink message to process
	 * @return Returns true if the message has been processed
	 */
	public boolean processMessage(MAVLinkMessage msg) {
		if (msg.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) {
			processReceivedParam((msg_param_value) msg);
			return true;
		}
		return false;
	}

	private void processReceivedParam(msg_param_value m_value) {
		// collect params in parameter list
		Parameter param = new Parameter(m_value);
		parameters.put((int) m_value.param_index, param);

		expectedParams = m_value.param_count;

		// update listener
		if (parameterListener != null)
			parameterListener.onParameterReceived(param, m_value.param_index, m_value.param_count);

		// Are all parameters here? Notify the listener with the parameters
		if (parameters.size() >= m_value.param_count) {
            parameterList.clear();
			for (int key : parameters.keySet()) {
				parameterList.add(parameters.get(key));
			}
			killWatchdog();
			myDrone.notifyDroneEvent(DroneEventsType.PARAMETERS_DOWNLOADED);

			if (parameterListener != null) {
				parameterListener.onEndReceivingParameters(parameterList);
			}
		} else {
			resetWatchdog();
		}
		myDrone.notifyDroneEvent(DroneEventsType.PARAMETER);
	}

	private void reRequestMissingParams(int howManyParams) {
		for (int i = 0; i < howManyParams; i++) {
			if (!parameters.containsKey(i)) {
				MavLinkParameters.readParameter(myDrone, i);
			}
		}
	}

	public void sendParameter(Parameter parameter) {
		MavLinkParameters.sendParameter(myDrone, parameter);
	}

	public void ReadParameter(String name) {
		MavLinkParameters.readParameter(myDrone, name);
	}

	public Parameter getParameter(String name) {
		for (int key : parameters.keySet()) {
			if (parameters.get(key).name.equalsIgnoreCase(name))
				return parameters.get(key);
		}
		return null;
	}

	public Parameter getLastParameter() {
		if (parameters.size() > 0)
			return parameters.get(parameters.size() - 1);

		return null;
	}

	private void onParameterStreamStopped() {
		reRequestMissingParams(expectedParams);
		resetWatchdog();
	}

	private void resetWatchdog() {
		watchdog.removeCallbacks(watchdogCallback);
		watchdog.postDelayed(watchdogCallback, TIMEOUT);
	}

	private void killWatchdog() {
		watchdog.removeCallbacks(watchdogCallback);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case HEARTBEAT_FIRST:
			if (!drone.getState().isFlying()) {
				refreshParameters();
			}
			break;
		case DISCONNECTED:
		case HEARTBEAT_TIMEOUT:
			killWatchdog();
			break;
		default:
			break;

		}
	}

	public void setParameterListener(DroneInterfaces.OnParameterManagerListener parameterListener) {
		this.parameterListener = parameterListener;
	}
}
