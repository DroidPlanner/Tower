package com.droidplanner.file.IO;

import android.util.Xml;
import android.view.View;
import com.droidplanner.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;


public class VehicleProfileReader {
    // vehicle profile tags
    private static final String TAG_METADATATYPE = "ParameterMetadataType";

    // view profile tags
    private static final String TAG_MISSIONVIEWPROFILES = "MissionViewProfile";
    private static final String TAG_VIEWPROFILE = "ViewProfile";
    private static final String TAG_DIALOGPROFILE = "DialogProfile";

    private static final String PREFIX_ID = "@+id/";
    private static final String PREFIX_DIALOG = "@dialog/";

    private static final String ATTR_ID = "id";
    private static final String ATTR_TEXT = "text";
    private static final String ATTR_VISIBILITY = "visibility";
    private static final String ATTR_TYPE = "type";

    private static final String ATTR_INC = "inc";
    private static final String ATTR_MIN = "min";
    private static final String ATTR_MAX = "max";

    // default tags
    private static final String TAG_DEFAULT = "Default";
    private static final String ATTR_WPNAV_SPEED = "wpNavSpeed";
    private static final String ATTR_MAX_ALTITUDE = "maxAltitude";


    public static void open(InputStream inputStream, VehicleProfile profile) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parse(parser, profile);

        } finally {
            try { inputStream.close(); } catch (IOException e) { /*nop*/ }
        }
    }

    private static void parse(XmlPullParser parser, VehicleProfile profile) throws XmlPullParserException, IOException {
        VehicleProfile.ViewProfileBuilder viewProfileBuilder = profile.getViewProfileBuilder();

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {

            final String parserName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if(parserName.equals(TAG_METADATATYPE)) {
                        // set metadata type
                        final String value = parser.getAttributeValue(null, ATTR_TYPE);
                        if(value != null)
                            profile.setParameterMetadataType(value);

                    } else if(parserName.equals(TAG_DEFAULT)) {
                        // set defaults
                        parseDefault(parser, profile.getDefault());

                    } else if(parserName.equals(TAG_VIEWPROFILE)) {
                        // add view profile if builder available
                        if(viewProfileBuilder != null)
                            viewProfileBuilder.addViewProfile(newViewProfile(parser));

                    } else if(parserName.equals(TAG_DIALOGPROFILE)) {
                        // add dialog profile, make active builder
                        viewProfileBuilder = profile.addDialogProfile(newDialogProfile(parser));
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if(parserName.equals(TAG_DIALOGPROFILE)) {
                        // make profile active builder
                        viewProfileBuilder = profile.getViewProfileBuilder();
                    }
                    break;
            }
            eventType = parser.next();
        }
    }

    // parse Default
    private static void parseDefault(XmlPullParser parser, VehicleProfile.Default default_) {
        // wpNavSpeed
        String value = parser.getAttributeValue(null, ATTR_WPNAV_SPEED);
        if(value != null)
            default_.setWpNavSpeed(parseInt(value));

        // maxAltitude
        value = parser.getAttributeValue(null, ATTR_MAX_ALTITUDE);
        if(value != null)
            default_.setMaxAltitude(parseInt(value));
    }

    // parse DialogProfile
    private static VehicleProfile.MissionDialogProfile newDialogProfile(XmlPullParser parser) {
        String attr = parser.getAttributeValue(null, ATTR_ID);
        if(attr == null)
            return null;

        final VehicleProfile.MissionDialogProfile dialogProfile = new VehicleProfile.MissionDialogProfile();

        // id
        int resId = findResId(attr);
        if(resId == -1)
            return null;
        dialogProfile.setResId(resId);

        return dialogProfile;
    }

    // parse ViewProfile
    private static VehicleProfile.MissionViewProfile newViewProfile(XmlPullParser parser) {
        String attr = parser.getAttributeValue(null, ATTR_ID);
        if(attr == null)
            return null;

        final VehicleProfile.MissionViewProfile viewProfile = new VehicleProfile.MissionViewProfile();

        // id
        int resId = findResId(attr);
        if(resId == -1)
            return null;
        viewProfile.setResId(resId);

        // text
        viewProfile.setText(parser.getAttributeValue(null, ATTR_TEXT));

        // visibiliy
        attr = parser.getAttributeValue(null, ATTR_VISIBILITY);
        if("gone".equalsIgnoreCase(attr)) {
            viewProfile.setVisibility(View.GONE);
        } else if("invisible".equalsIgnoreCase(attr)) {
            viewProfile.setVisibility(View.INVISIBLE);
        } else {
            viewProfile.setVisibility(View.VISIBLE);
        }

        // type
        viewProfile.setType(parser.getAttributeValue(null, ATTR_TYPE));

        // min, max, inc
        viewProfile.setMin(parseDouble(parser.getAttributeValue(null, ATTR_MIN)));
        viewProfile.setMax(parseDouble(parser.getAttributeValue(null, ATTR_MAX)));
        viewProfile.setInc(parseDouble(parser.getAttributeValue(null, ATTR_INC)));

        return viewProfile;
    }

    private static int findResId(String idName) {
        final Class resClass;
        if(idName.startsWith(PREFIX_ID)) {
            // R.id
            resClass = R.id.class;
            idName = idName.substring(PREFIX_ID.length());

        } else if(idName.startsWith(PREFIX_DIALOG)) {
            // R.layout
            resClass = R.layout.class;
            idName = idName.substring(PREFIX_DIALOG.length());

        } else {
            // not found
            return -1;
        }

        // idName -> resId
        try {
            final Field field = resClass.getDeclaredField(idName);
            return field.getInt(field);
        } catch (Exception e) {
            return -1;
        }
    }

    private static int parseInt(String str) {
        if(str == null)
            return 0;

        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
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
