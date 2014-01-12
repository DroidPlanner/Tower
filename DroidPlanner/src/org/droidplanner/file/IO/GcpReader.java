package org.droidplanner.file.IO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.droidplanner.dialogs.openfile.OpenFileDialog.FileReader;
import org.droidplanner.file.DirectoryPath;
import org.droidplanner.file.FileList;
import org.droidplanner.gcp.Gcp;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;


/**
 * Class to parse a Kml file, based on the code from
 * http://developer.android.com/training/basics/network-ops/xml.html
 * 
 */
public class GcpReader implements FileReader {
	private final String ns = null;

	public List<Gcp> gcpList;

	public boolean openGCPFile(String fileWithPath) {
		boolean returnValue = false;
		if (fileWithPath.endsWith(".kmz")) {
			returnValue = openKMZ(fileWithPath);
		} else if (fileWithPath.endsWith(".kml")) {
			returnValue = openKML(fileWithPath);
		}
		return returnValue;
	}

	private boolean openKML(String fileWithPath) {
		try {
			FileInputStream in = new FileInputStream(fileWithPath);

			gcpList = parse(in);
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

	private boolean openKMZ(String fileWithPath) {
		try {
			ZipInputStream zin = new ZipInputStream(new FileInputStream(
					fileWithPath));
			ZipEntry ze;
			while ((ze = zin.getNextEntry()) != null) {
				if (ze.getName().contains(".kml")) {
					gcpList = parse(zin);
				}
			}
			zin.close();
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

	public List<Gcp> parse(InputStream in) throws XmlPullParserException,
			IOException {
		gcpList = new ArrayList<Gcp>();
		XmlPullParser parser = Xml.newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(in, null);
		parser.nextTag();
		readFeed(parser);
		return gcpList;
	}

	private void readFeed(XmlPullParser parser) throws XmlPullParserException,
			IOException {

		parser.require(XmlPullParser.START_TAG, ns, "kml");
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("Placemark")) {
				readPlacemark(parser);
			}
		}
	}

	private void readPlacemark(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "Placemark");
		Gcp point = null;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("Point")) {
				point = readPoint(parser);
				if (point != null) {
					gcpList.add(point);
				}
			} else {
				skip(parser);
			}
		}
	}

	// Processes Point tags in the feed.
	private Gcp readPoint(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		Gcp point = null;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("coordinates")) {
				point = readCoordinate(parser);
			} else {
				skip(parser);
			}
		}
		return point;
	}

	private Gcp readCoordinate(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		Double Lat, Lng;

		parser.require(XmlPullParser.START_TAG, ns, "coordinates");
		String coordString = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "coordinates");

		String title[] = coordString.split(",");
		Lng = Double.valueOf(title[0]);
		Lat = Double.valueOf(title[1]);

		return (new Gcp(Lat, Lng));
	}

	// For the tags title and summary, extracts their text values.
	private String readText(XmlPullParser parser) throws IOException,
			XmlPullParserException {
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
	private void skip(XmlPullParser parser) throws XmlPullParserException,
			IOException {
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
		return DirectoryPath.getGCPPath();
	}

	@Override
	public String[] getFileList() {
		return FileList.getKMZFileList();
	}

	@Override
	public boolean openFile(String filenameWithPath) {
		return openGCPFile(filenameWithPath);
	}
}