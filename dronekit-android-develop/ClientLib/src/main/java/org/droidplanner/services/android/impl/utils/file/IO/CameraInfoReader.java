package org.droidplanner.services.android.impl.utils.file.IO;

import android.util.Xml;

import org.droidplanner.services.android.impl.core.survey.CameraInfo;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class to parse a Kml file, based on the code from
 * http://developer.android.com/training/basics/network-ops/xml.html
 * 
 */
public class CameraInfoReader {

	private XmlPullParser parser;

	private CameraInfo cameraInfo = new CameraInfo();

	public void openFile(InputStream inputStream) throws Exception {
		parse(inputStream);
		inputStream.close();
	}

	public CameraInfo getCameraInfo() {
		return cameraInfo;
	}

	public void parse(InputStream in) throws XmlPullParserException, IOException {
		parser = Xml.newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(in, null);
		parser.nextTag();
		readCameraInfo();
	}

	private void readCameraInfo() throws XmlPullParserException, IOException {

		parser.require(XmlPullParser.START_TAG, null, "cameraInfo");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("SensorWidth")) {
				cameraInfo.sensorWidth = readDouble("SensorWidth");
			} else if (name.equals("SensorHeight")) {
				cameraInfo.sensorHeight = readDouble("SensorHeight");
			} else if (name.equals("SensorResolution")) {
				cameraInfo.sensorResolution = readDouble("SensorResolution");
			} else if (name.equals("FocalLength")) {
				cameraInfo.focalLength = readDouble("FocalLength");
			} else if (name.equals("Overlap")) {
				cameraInfo.overlap = readDouble("Overlap");
			} else if (name.equals("Sidelap")) {
				cameraInfo.sidelap = readDouble("Sidelap");
			} else if (name.equals("Name")) {
				cameraInfo.name = readString("Name");
			} else if (name.equals("Orientation")) {
				cameraInfo.isInLandscapeOrientation = !readText().equals("Portrait");
			} else {
				skip();
			}
		}
	}

	private String readString(String entry) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, entry);
		String value = readText();
		parser.require(XmlPullParser.END_TAG, null, entry);
		return value;
	}

	private Double readDouble(String entry) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, null, entry);
		Double value = Double.valueOf(readText());
		parser.require(XmlPullParser.END_TAG, null, entry);
		return value;
	}

	// For the tags title and summary, extracts their text values.
	private String readText() throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	// Skips tags the parser isn't interested in. Uses depth to handle
	// nested tags. i.e.,
	// if the next tag after a START_TAG isn't a matching END_TAG, it keeps
	// going until it
	// finds the matching END_TAG (as indicated by the value of "depth"
	// being 0).
	private void skip() throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}
}