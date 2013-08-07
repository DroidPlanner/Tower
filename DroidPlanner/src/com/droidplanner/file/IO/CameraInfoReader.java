package com.droidplanner.file.IO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

import com.droidplanner.dialogs.OpenFileDialog.FileReader;
import com.droidplanner.file.DirectoryPath;
import com.droidplanner.file.FileList;

/**
 * Class to parse a Kml file, based on the code from
 * http://developer.android.com/training/basics/network-ops/xml.html
 * 
 */
public class CameraInfoReader implements FileReader {

	private XmlPullParser parser;
	private Double imageWidth;
	private Double imageHeight;
	private Double focalLength;
	private Double overlap;
	private Double sidelap;
	private Object isInLandscapeOrientation;

	public boolean openCameraInfoFile(String fileWithPath) {
		return openKML(fileWithPath);
	}

	private boolean openKML(String fileWithPath) {
		try {
			FileInputStream in = new FileInputStream(fileWithPath);
			parse(in);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void parse(InputStream in) throws XmlPullParserException,
			IOException {
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
			if (name.equals("ImageWidth")) {
				imageWidth = readDouble("ImageWidth");
				Log.d("PARSER", "ImageWidth");
			} else if (name.equals("ImageHeight")) {
				imageHeight = readDouble("ImageHeight");
				Log.d("PARSER", "ImageHeight");
			} else if (name.equals("FocalLength")) {
				focalLength = readDouble("FocalLength");
				Log.d("PARSER", "FocalLength");
			} else if (name.equals("Overlap")) {
				overlap = readDouble("Overlap");
				Log.d("PARSER", "Overlap");
			} else if (name.equals("Sidelap")) {
				sidelap = readDouble("Sidelap");
				Log.d("PARSER", "Sidelap");
			} else if (name.equals("Orientation")) {
				isInLandscapeOrientation = readText().equals("Portrait")?false:true;
				Log.d("PARSER", "Orientation");
			} else {
				skip();
			}
		}
	}

	private Double readDouble(String entry) throws IOException,
			XmlPullParserException {
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

	@Override
	public String getPath() {
		return DirectoryPath.getCameraInfoPath();
	}

	@Override
	public String[] getFileList() {
		return FileList.getCameraInfoFileList();
	}

	@Override
	public boolean openFile(String filenameWithPath) {
		return openCameraInfoFile(filenameWithPath);
	}
}