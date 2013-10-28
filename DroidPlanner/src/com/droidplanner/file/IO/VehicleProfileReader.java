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
import java.util.List;


public class VehicleProfileReader {
    private static final String TAG_METADATATYPE = "ParameterMetadataType";
    private static final String TAG_VIEWPROFILE = "ViewProfile";
    private static final String TAG_DIALOGPROFILE = "DialogProfile";
    private static final String TAG_MISSIONVIEWPROFILES = "MissionViewProfile";

    private static final String ATTR_ID = "id";
    private static final String ATTR_TEXT = "text";
    private static final String ATTR_VISIBILITY = "visibility";
    private static final String ATTR_TYPE = "type";

    private static final String ATTR_INC = "inc";
    private static final String ATTR_MIN = "min";
    private static final String ATTR_MAX = "max";

    private static final String PREFIX_ID = "@+id/";
    private static final String PREFIX_DIALOG = "@dialog/";


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
        List<MissionViewProfile> viewProfiles = profile.getMissionViewProfiles();

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {

            final String parserName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if(parserName.equals(TAG_METADATATYPE)) {
                        // set metadata type
                        profile.setParameterMetadataType(parser.getAttributeValue(null, ATTR_TYPE));

                    } else if(parserName.equals(TAG_VIEWPROFILE)) {
                        // add view profile
                        final MissionViewProfile viewProfile = newViewProfile(parser);
                        if(viewProfile != null)
                            viewProfiles.add(viewProfile);

                    } else if(parserName.equals(TAG_DIALOGPROFILE)) {
                        // map dialog profile, attach viewProfiles
                        final MissionDialogProfile dialogProfile = newDialogProfile(parser);
                        if(dialogProfile != null) {
                            profile.getProfileMissionDialogs().put(dialogProfile.getResId(), dialogProfile);
                            viewProfiles = dialogProfile.getMissionViewProfiles();
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if(parserName.equals(TAG_DIALOGPROFILE)) {
                        // detach viewProfiles
                        viewProfiles = profile.getMissionViewProfiles();
                    }
                    break;
            }
            eventType = parser.next();
        }
        return profile;
    }

    private static MissionDialogProfile newDialogProfile(XmlPullParser parser) {
        String attr = parser.getAttributeValue(null, ATTR_ID);
        if(attr == null)
            return null;

        final MissionDialogProfile dialogProfile = new MissionDialogProfile();

        // id
        int resId = findResId(attr);
        if(resId == -1)
            return null;
        dialogProfile.setResId(resId);

        return dialogProfile;
    }

    private static MissionViewProfile newViewProfile(XmlPullParser parser) {
        String attr = parser.getAttributeValue(null, ATTR_ID);
        if(attr == null)
            return null;

        final MissionViewProfile viewProfile = new MissionViewProfile();

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
