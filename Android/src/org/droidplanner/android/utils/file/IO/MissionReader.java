package org.droidplanner.android.utils.file.IO;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.utils.file.DirectoryPath;
import org.droidplanner.android.utils.file.FileList;
import org.droidplanner.android.utils.file.FileManager;

import com.ox3dr.services.android.lib.drone.mission.item.raw.MissionItemMessage;

/**
 * Read msg_mission_item list as...
 * 
 * QGC WPL <VERSION> <INDEX> <CURRENT WP> <COORD FRAME> <COMMAND> <PARAM1>
 * <PARAM2> <PARAM3> <PARAM4> <PARAM5/X/LONGITUDE> <PARAM6/Y/LATITUDE>
 * <PARAM7/Z/ALTITUDE> <AUTOCONTINUE>
 * 
 * See http://qgroundcontrol.org/mavlink/waypoint_protocol for details
 * 
 */
public class MissionReader implements OpenFileDialog.FileReader {

	private List<MissionItemMessage> msgMissionItems;

	public MissionReader() {
		this.msgMissionItems = new ArrayList<MissionItemMessage>();
	}

	public boolean openMission(String file) {
		if (!FileManager.isExternalStorageAvailable()) {
			return false;
		}
		try {
			final FileInputStream in = new FileInputStream(file);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			if (!isWaypointFile(reader)) {
				in.close();
				return false;
			}
			parseLines(reader);

			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public List<MissionItemMessage> getMsgMissionItems() {
		return msgMissionItems;
	}

	private void parseLines(BufferedReader reader) throws IOException {
		msgMissionItems.clear();

		// for all lines
		String line;
		while ((line = reader.readLine()) != null) {
			// parse line (TAB delimited)
			final String[] RowData = line.split("\t");

			final MissionItemMessage msg = new MissionItemMessage();
			msg.setSeq(Short.parseShort(RowData[0]));
			msg.setCurrent(Byte.parseByte(RowData[1]));
			msg.setFrame(Byte.parseByte(RowData[2]));
			msg.setCommand(Short.parseShort(RowData[3]));

			msg.setParam1(Float.parseFloat(RowData[4]));
			msg.setParam2(Float.parseFloat(RowData[5]));
			msg.setParam3(Float.parseFloat(RowData[6]));
			msg.setParam4(Float.parseFloat(RowData[7]));

			msg.setX(Float.parseFloat(RowData[8]));
			msg.setY(Float.parseFloat(RowData[9]));
			msg.setZ(Float.parseFloat(RowData[10]));

			msg.setAutocontinue(Byte.parseByte(RowData[11]));

			msgMissionItems.add(msg);
		}

	}

	private static boolean isWaypointFile(BufferedReader reader) throws IOException {
		return reader.readLine().contains("QGC WPL 110");
	}

	@Override
	public String getPath() {
		return DirectoryPath.getWaypointsPath();
	}

	@Override
	public String[] getFileList() {
		return FileList.getWaypointFileList();
	}

	@Override
	public boolean openFile(String file) {
		return openMission(file);
	}
}
