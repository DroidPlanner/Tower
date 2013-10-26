package com.droidplanner.file.IO;

import android.util.Xml;
import android.view.View;
import com.droidplanner.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;


public class VehicleProfileReader {
    private static final String TAG_METADATATYPE = "MetadataType";
    private static final String TAG_PARAMETER = "MissionParameter";
    private static final String TAG_PARAMETERS = "MissionParameters";

    private static final String ATTR_ID = "id";
    private static final String ATTR_VISIBILITY = "visibility";
    private static final String ATTR_TYPE = "type";

    private static final String ATTR_INC = "inc";
    private static final String ATTR_MIN = "min";
    private static final String ATTR_MAX = "max";


    public static VehicleProfile open(InputStream inputStream) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            return parse(parser);

        } finally {
            try { inputStream.close(); } catch (IOException e) { /*nop*/ }
        }
    }

    private static VehicleProfile parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        final VehicleProfile profile = new VehicleProfile();
        final ArrayList<VehicleMissionParameter> parameters = new ArrayList<VehicleMissionParameter>();
        profile.setMissionParameters(parameters);

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if(parser.getName().equals(TAG_METADATATYPE)) {
                        profile.setMetadataType(parser.getAttributeValue(null, ATTR_TYPE));

                    } else if(parser.getName().equals(TAG_PARAMETER)) {
                        final VehicleMissionParameter parameter = newParameter(parser);
                        if(parameter != null)
                            parameters.add(parameter);
                    }
                    break;
            }
            eventType = parser.next();
        }
        return profile;
    }

    private static VehicleMissionParameter newParameter(XmlPullParser parser) {
        String attr = parser.getAttributeValue(null, ATTR_ID);
        if(attr == null)
            return null;
        final VehicleMissionParameter parameter = new VehicleMissionParameter();

        // id
        try {
            final Field f = R.id.class.getDeclaredField(attr);
            parameter.setResId(f.getInt(f));
        } catch (Exception e) {
            return null;
        }

        // visibiliy
        attr = parser.getAttributeValue(null, ATTR_VISIBILITY);
        if("gone".equalsIgnoreCase(attr)) {
            parameter.setVisibility(View.GONE);
        } else if("invisible".equalsIgnoreCase(attr)) {
            parameter.setVisibility(View.INVISIBLE);
        } else {
            parameter.setVisibility(View.VISIBLE);
        }

        // type
        parameter.setType(parser.getAttributeValue(null, ATTR_TYPE));

        // min, max, inc
        parameter.setMin(parseDouble(parser.getAttributeValue(null, ATTR_MIN)));
        parameter.setMax(parseDouble(parser.getAttributeValue(null, ATTR_MAX)));
        parameter.setInc(parseDouble(parser.getAttributeValue(null, ATTR_INC)));

        return parameter;
    }

    private static double parseDouble(String str) {
        if(str == null)
            return 0;

        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
