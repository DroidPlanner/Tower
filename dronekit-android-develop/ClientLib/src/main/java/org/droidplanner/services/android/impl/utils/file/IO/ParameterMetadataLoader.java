package org.droidplanner.services.android.impl.utils.file.IO;

import android.content.Context;
import android.util.Xml;

import org.droidplanner.services.android.impl.core.drone.profiles.ParameterMetadata;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by fhuya on 10/29/14.
 */
public class ParameterMetadataLoader {

    private static final String PARAMETERMETADATA_PATH = "Parameters/ParameterMetaData.xml";

    private static final String METADATA_DISPLAYNAME = "DisplayName";
    private static final String METADATA_DESCRIPTION = "Description";
    private static final String METADATA_UNITS = "Units";
    private static final String METADATA_VALUES = "Values";
    private static final String METADATA_RANGE = "Range";

    public static void load(Context context, String metadataType, Map<String, ParameterMetadata> metadata)
            throws IOException, XmlPullParserException {
        InputStream inputStream = context.getAssets().open(PARAMETERMETADATA_PATH);
        open(inputStream, metadataType, metadata);
    }

    private static void open(InputStream inputStream, String metadataType, Map<String, ParameterMetadata> metadataMap)
            throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parseMetadata(parser, metadataType, metadataMap);

        } finally {
            try {
                inputStream.close();
            } catch (IOException e) { /* nop */
            }
        }
    }

    private static void parseMetadata(XmlPullParser parser, String metadataType, Map<String, ParameterMetadata> metadataMap)
            throws XmlPullParserException, IOException {
        String name;
        boolean parsing = false;
        ParameterMetadata metadata = null;
        metadataMap.clear();

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    // name == metadataType: start collecting metadata(s)
                    // metadata == null: create new metadata w/ name
                    // metadata != null: add to metadata as property
                    if (metadataType.equals(name)) {
                        parsing = true;
                    } else if (parsing) {
                        if (metadata == null) {
                            metadata = new ParameterMetadata();
                            metadata.setName(name);
                        } else {
                            addMetaDataProperty(metadata, name, parser.nextText());
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    // name == metadataType: done
                    // name == metadata.name: add metadata to metadataMap
                    if (metadataType.equals(name)) {
                        return;
                    } else if (metadata != null && metadata.getName().equals(name)) {
                        metadataMap.put(metadata.getName(), metadata);
                        metadata = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
        // no metadata
    }

    private static void addMetaDataProperty(ParameterMetadata metaData, String name, String text) {
        switch (name) {
            case METADATA_DISPLAYNAME:
                metaData.setDisplayName(text);
                break;
            case METADATA_DESCRIPTION:
                metaData.setDescription(text);
                break;
            case METADATA_UNITS:
                metaData.setUnits(text);
                break;
            case METADATA_RANGE:
                metaData.setRange(text);
                break;
            case METADATA_VALUES:
                metaData.setValues(text);
                break;
        }
    }
}
