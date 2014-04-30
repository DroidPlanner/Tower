package org.droidplanner.android.utils.file.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.drone.variables.Type.FirmwareType;
import org.droidplanner.android.utils.file.AssetUtil;
import org.droidplanner.android.utils.file.DirectoryPath;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Xml;

public class VehicleProfileReader {
	private static final String VEHICLEPROFILE_PATH = "VehicleProfiles";

	// vehicle profile tags
	private static final String TAG_METADATATYPE = "ParameterMetadataType";

	// default tags
	private static final String TAG_DEFAULT = "Default";
	private static final String ATTR_WPNAV_SPEED = "wpNavSpeed";
	private static final String ATTR_MAX_ALTITUDE = "maxAltitude";

	private static final String ATTR_TYPE = "type";

	/**
	 * Load/aggregate profile from resources and file (if available) File will
	 * override resource settings
	 */
	public static VehicleProfile load(Context context, FirmwareType vehicleType) {
		final String fileName = vehicleType + ".xml";
		final String path = VEHICLEPROFILE_PATH + File.separator + fileName;

		try {
			final VehicleProfile newProfile = new VehicleProfile();

			// load profile from resources first
			final AssetManager assetManager = context.getAssets();
			if (AssetUtil.exists(assetManager, VEHICLEPROFILE_PATH, fileName)) {
				final InputStream inputStream = assetManager.open(path);
				VehicleProfileReader.open(inputStream, newProfile);
			}

			// load (override) from file if available
			final File file = new File(DirectoryPath.getDroidPlannerPath()
					+ path);
			if (file.exists()) {
				final InputStream inputStream = new FileInputStream(file);
				VehicleProfileReader.open(inputStream, newProfile);
			}
			return newProfile;

		} catch (Exception e) {
			// nop
		}
		return null;
	}

	private static void open(InputStream inputStream, VehicleProfile profile)
			throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(inputStream, null);
			parse(parser, profile);

		} finally {
			try {
				inputStream.close();
			} catch (IOException e) { /* nop */
			}
		}
	}

	private static void parse(XmlPullParser parser, VehicleProfile profile)
			throws XmlPullParserException, IOException {

		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {

			final String parserName = parser.getName();
			switch (eventType) {
			case XmlPullParser.START_TAG:
				if (parserName.equals(TAG_METADATATYPE)) {
					// set metadata type
					final String value = parser.getAttributeValue(null,
							ATTR_TYPE);
					if (value != null)
						profile.setParameterMetadataType(value);

				} else if (parserName.equals(TAG_DEFAULT)) {
					// set defaults
					parseDefault(parser, profile.getDefault());

				}
				break;

			case XmlPullParser.END_TAG:
				break;
			}
			eventType = parser.next();
		}
	}

	// parse Default
	private static void parseDefault(XmlPullParser parser,
			VehicleProfile.Default default_) {
		// wpNavSpeed
		String value = parser.getAttributeValue(null, ATTR_WPNAV_SPEED);
		if (value != null)
			default_.setWpNavSpeed(parseInt(value));

		// maxAltitude
		value = parser.getAttributeValue(null, ATTR_MAX_ALTITUDE);
		if (value != null)
			default_.setMaxAltitude(parseInt(value));
	}

	private static int parseInt(String str) {
		if (str == null)
			return 0;

		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static double parseDouble(String str) {
		if (str == null)
			return 0;

		try {
			return Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
