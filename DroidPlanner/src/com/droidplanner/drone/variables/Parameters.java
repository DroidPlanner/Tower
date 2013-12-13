package com.droidplanner.drone.variables;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_param_value;
import com.droidplanner.MAVLink.MavLinkParameters;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces;
import com.droidplanner.drone.DroneVariable;
import com.droidplanner.file.DirectoryPath;
import com.droidplanner.file.IO.ParameterMetadataMap;
import com.droidplanner.file.IO.ParameterMetadataMapReader;
import com.droidplanner.file.IO.VehicleProfile;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterMetadata;

/**
 * Class to manage the communication of parameters to the MAV.
 * 
 * Should be initialized with a MAVLink Object, so the manager can send messages
 * via the MAV link. The function processMessage must be called with every new
 * MAV Message.
 * 
 */
public class Parameters extends DroneVariable {
    private static final String PARAMETERMETADATA_PATH = "Parameters/ParameterMetaData.xml";

    private List<Parameter> parameters = new ArrayList<Parameter>();
    private ParameterMetadataMap metadataMap;

	public DroneInterfaces.OnParameterManagerListner parameterListner;

	public Parameters(Drone myDrone) {
		super(myDrone);
	}

	public void getAllParameters() {
		parameters.clear();
		if(parameterListner!=null)
			parameterListner.onBeginReceivingParameters();

		MavLinkParameters.requestParametersList(myDrone);
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
		parameters.add(param);

		// update listener
		if(parameterListner!=null)
			parameterListner.onParameterReceived(param, m_value.param_index, m_value.param_count);

		// last param? notify listener w/ params
		if (m_value.param_index == m_value.param_count - 1) {
			if(parameterListner!=null)
				parameterListner.onEndReceivingParameters(parameters);
		}
	}

	public void sendParameter(Parameter parameter) {
		MavLinkParameters.sendParameter(myDrone, parameter);
	}


    public ParameterMetadata getMetadata(String name) {
        return (metadataMap == null) ? null : metadataMap.get(name);
    }

    public void loadMetadata() {
        metadataMap = null;

        // get metadata type from profile, bail if none
        final String metadataType;
        final VehicleProfile profile = myDrone.profile.getProfile();
        if(profile == null || (metadataType = profile.getParameterMetadataType()) == null)
            return;

        try {
            // use user supplied file in ~/Parameters if available, else fallback to asset from resources
            final InputStream inputStream;
            final File file = new File(DirectoryPath.getDroidPlannerPath() + PARAMETERMETADATA_PATH);
            if(file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = myDrone.context.getAssets().open(PARAMETERMETADATA_PATH);
            }
            // load
            metadataMap = ParameterMetadataMapReader.open(inputStream, metadataType);

        } catch (Exception ex) {
            // nop
        }
    }
    
	public void ReadParameter(String name) {
		MavLinkParameters.readParameter(myDrone, name);
	}
}
