package org.droidplanner.android.utils.file.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.droidplanner.android.utils.file.DirectoryPath;
import org.droidplanner.core.parameters.ParameterMetadata;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Xml;

/**
 * Parameter metadata parser extracted from parameters
 * 
 */
public class ParameterMetadataMapReader {

	private static final String PARAMETERMETADATA_PATH = "Parameters/ParameterMetaData.xml";

	private static final String METADATA_DISPLAYNAME = "DisplayName";
	private static final String METADATA_DESCRIPTION = "Description";
	private static final String METADATA_UNITS = "Units";
	private static final String METADATA_VALUES = "Values";
	private static final String METADATA_RANGE = "Range";

	public static Map<String, ParameterMetadata> load(Context context,
			String metadataType) throws IOException, XmlPullParserException {
		// use user supplied file in ~/Parameters if available, else fallback to
		// asset from resources
		final InputStream inputStream;
		final File file = new File(DirectoryPath.getDroidPlannerPath()
				+ PARAMETERMETADATA_PATH);
		if (file.exists()) {
			inputStream = new FileInputStream(file);
		} else {
			inputStream = context.getAssets().open(PARAMETERMETADATA_PATH);
		}
		return open(inputStream, metadataType);
	}

	private static ParameterMetadataMap open(InputStream inputStream,
			String metadataType) throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(inputStream, null);
			return parseMetadata(parser, metadataType);

		} finally {
			try {
				inputStream.close();
			} catch (IOException e) { /* nop */
			}
		}
	}

	private static ParameterMetadataMap parseMetadata(XmlPullParser parser,
			String metadataType) throws XmlPullParserException, IOException {
		String name;
		boolean parsing = false;
		ParameterMetadata metadata = null;
		ParameterMetadataMap metadataMap = new ParameterMetadataMap();

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
					return metadataMap;
				} else if (metadata != null && metadata.getName().equals(name)) {
					metadataMap.put(metadata.getName(), metadata);
					metadata = null;
				}
				break;
			}
			eventType = parser.next();
		}
		// no metadata
		return null;
	}

	private static void addMetaDataProperty(ParameterMetadata metaData,
			String name, String text) {
		if (name.equals(METADATA_DISPLAYNAME))
			metaData.setDisplayName(text);
		else if (name.equals(METADATA_DESCRIPTION))
			metaData.setDescription(text);

		else if (name.equals(METADATA_UNITS))
			metaData.setUnits(text);
		else if (name.equals(METADATA_RANGE))
			metaData.setRange(text);
		else if (name.equals(METADATA_VALUES))
			metaData.setValues(text);
	}
}
