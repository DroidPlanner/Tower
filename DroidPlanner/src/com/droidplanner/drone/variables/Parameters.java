package com.droidplanner.drone.variables;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_param_value;
import com.droidplanner.MAVLink.MavLinkParameters;
import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces;
import com.droidplanner.drone.DroneVariable;
import com.droidplanner.file.DirectoryPath;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterMetadata;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Class to manage the communication of parameters to the MAV.
 * 
 * Should be initialized with a MAVLink Object, so the manager can send messages
 * via the MAV link. The function processMessage must be called with every new
 * MAV Message.
 * 
 */
public class Parameters extends DroneVariable {
    private static final String METADATA_DISPLAYNAME = "DisplayName";
    private static final String METADATA_DESCRIPTION = "Description";
    private static final String METADATA_UNITS = "Units";
    private static final String METADATA_VALUES = "Values";

    private List<Parameter> parameters = new ArrayList<Parameter>();
    private Map<String, ParameterMetadata> metaDataMap;

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

    public void notifyParameterMetadataChanged() {
        if(parameterListner != null)
            parameterListner.onParamterMetaDataChanged();
    }

    public ParameterMetadata getMetaData(String name) {
        return (metaDataMap == null) ? null : metaDataMap.get(name);
    }

    public void loadMetaData(Context context) {
        metaDataMap = null;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String paramMetaElt = prefs.getString("pref_param_metadata", null);
        if(paramMetaElt == null || paramMetaElt.equals(context.getString(R.string.none)))
            return;

        File file = new File(DirectoryPath.getParameterMetadataPath());
        if(!file.exists())
            return;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);

            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(fis, null);
            parseMetaData(parser, paramMetaElt);

        } catch (Exception ex) {
            // nop

        } finally {
            if(fis != null) {
                try { fis.close(); } catch (IOException e) { /*nop*/ }
            }
        }
    }

    private void parseMetaData(XmlPullParser parser, String paramMetaElt) throws XmlPullParserException, IOException {
        String name;
        boolean parsing = false;
        ParameterMetadata metaData = null;
        Map<String, ParameterMetadata> metaDataMap = new HashMap<String, ParameterMetadata>();

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if(paramMetaElt.equals(name)) {
                        // start collecting metadata
                        parsing = true;
                    } else if(parsing) {
                        if(metaData == null) {
                            // new metadata element
                            metaData = new ParameterMetadata();
                            metaData.setName(name);
                        } else {
                            addMetaDataProperty(metaData, name, parser.nextText());
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if(paramMetaElt.equals(name)) {
                        // done. keep metadata
                        this.metaDataMap = metaDataMap;
                        return;
                    } else if(metaData != null && metaData.getName().equals(name)) {
                        // commit metadata to map
                        metaDataMap.put(metaData.getName(), metaData);
                        metaData = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
    }

    private void addMetaDataProperty(ParameterMetadata metaData, String name, String text) {
        if(name.equals(METADATA_DISPLAYNAME))
            metaData.setDisplayName(text);
        else if(name.equals(METADATA_DESCRIPTION))
            metaData.setDescription(text);

        else if(name.equals(METADATA_UNITS))
            metaData.setUnits(text);
        else if(name.equals(METADATA_VALUES))
            metaData.setValues(text);
    }
}
