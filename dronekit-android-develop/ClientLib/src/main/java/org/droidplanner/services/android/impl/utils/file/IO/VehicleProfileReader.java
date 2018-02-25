package org.droidplanner.services.android.impl.utils.file.IO;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Xml;

import org.droidplanner.services.android.impl.core.drone.profiles.VehicleProfile;
import org.droidplanner.services.android.impl.core.firmware.FirmwareType;
import org.droidplanner.services.android.impl.utils.file.AssetUtil;
import org.droidplanner.services.android.impl.utils.file.DirectoryPath;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
			VehicleProfile newProfile = new VehicleProfile();

			File file = new File(DirectoryPath.getPublicDataPath(context) + path);
			if (file.exists()) {
				loadProfileFromFile(newProfile, file);
			} else {
				loadProfileFromResources(context, fileName, path, newProfile);
			}
			return newProfile;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void loadProfileFromFile(final VehicleProfile newProfile, final File file)
			throws FileNotFoundException, XmlPullParserException, IOException {
		final InputStream inputStream = new FileInputStream(file);
		VehicleProfileReader.open(inputStream, newProfile);
	}

	private static void loadProfileFromResources(Context context, final String fileName,
			final String path, final VehicleProfile newProfile) throws IOException,
			XmlPullParserException {
		final AssetManager assetManager = context.getAssets();
		if (AssetUtil.exists(assetManager, VEHICLEPROFILE_PATH, fileName)) {
			final InputStream inputStream = assetManager.open(path);
			VehicleProfileReader.open(inputStream, newProfile);
		}
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
					final String value = parser.getAttributeValue(null, ATTR_TYPE);
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
	private static void parseDefault(XmlPullParser parser, VehicleProfile.Default default_) {
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
}
