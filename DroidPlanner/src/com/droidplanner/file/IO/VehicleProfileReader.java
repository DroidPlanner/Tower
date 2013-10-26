package com.droidplanner.file.IO;

import android.app.Application;
import android.content.res.Resources;
import android.util.Xml;
import android.view.View;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.parameters.ParameterMetadata;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;


public class VehicleProfileReader {
    private static final String TAG_PARAMETER = "Parameter";
    private static final String TAG_PARAMETERS = "Parameters";

    private static final String TAG_ID = "id";
    private static final String TAG_VISIBILITY = "visibility";
    private static final String TAG_TYPE = "type";

    private static final String TAG_INC = "inc";
    private static final String TAG_MIN = "min";
    private static final String TAG_MAX = "max";


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
        final ArrayList<VehicleParameter> parameters = new ArrayList<VehicleParameter>();
        profile.setParameters(parameters);

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if(parser.getName().equals(TAG_PARAMETER)) {
                        final VehicleParameter parameter = newParameter(parser);
                        if(parameter != null)
                            parameters.add(parameter);
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if(parser.getName().equals(TAG_PARAMETERS)) {
                        return profile;
                    }
                    break;
            }
            eventType = parser.next();
        }
        // no parameter
        return null;
    }

    private static VehicleParameter newParameter(XmlPullParser parser) {
        String attr = parser.getAttributeValue(null, TAG_ID);
        if(attr == null)
            return null;
        final VehicleParameter parameter = new VehicleParameter();

        // id
        try {
            final Field f = R.id.class.getDeclaredField(attr);
            parameter.setResId(f.getInt(f));
        } catch (Exception e) {
            return null;
        }

        // visibiliy
        attr = parser.getAttributeValue(null, TAG_VISIBILITY);
        if("gone".equalsIgnoreCase(attr)) {
            parameter.setVisibility(View.GONE);
        } else if("invisible".equalsIgnoreCase(attr)) {
            parameter.setVisibility(View.INVISIBLE);
        } else {
            parameter.setVisibility(View.VISIBLE);
        }

        // type
        parameter.setType(parser.getAttributeValue(null, TAG_TYPE));

        // min, max, inc
        parameter.setMin(parseDouble(parser.getAttributeValue(null, TAG_MIN)));
        parameter.setMax(parseDouble(parser.getAttributeValue(null, TAG_MAX)));
        parameter.setInc(parseDouble(parser.getAttributeValue(null, TAG_INC)));

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
