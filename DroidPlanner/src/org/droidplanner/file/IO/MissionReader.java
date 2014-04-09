package org.droidplanner.file.IO;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.droidplanner.dialogs.openfile.OpenFileDialog.FileReader;
import org.droidplanner.file.DirectoryPath;
import org.droidplanner.file.FileList;
import org.droidplanner.file.FileManager;

public class MissionReader implements FileReader {

    private List<msg_mission_item> msgMissionItems;

	public MissionReader() {
		this.msgMissionItems = new ArrayList<msg_mission_item>();
	}

	public boolean openMission(String file) {
		if (!FileManager.isExternalStorageAvaliable()) {
			return false;
		}
		try {
			FileInputStream in = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));

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

	public List<msg_mission_item> getMsgMissionItems() {
		return msgMissionItems;
	}

	private void parseLines(BufferedReader reader) throws IOException {
        msgMissionItems.clear();

        String line;
        while ((line = reader.readLine()) != null) {
			final String[] RowData = line.split("\t");

            final msg_mission_item msg = new msg_mission_item();
            msg.seq = Short.parseShort(RowData[0]);
            msg.current = Byte.parseByte(RowData[1]);
            msg.frame = Byte.parseByte(RowData[2]);
            msg.command = Short.parseShort(RowData[3]);

            msg.param1 = Float.parseFloat(RowData[4]);
            msg.param2 = Float.parseFloat(RowData[5]);
            msg.param3 = Float.parseFloat(RowData[6]);
            msg.param4 = Float.parseFloat(RowData[7]);

            msg.x = Float.parseFloat(RowData[8]);
            msg.y = Float.parseFloat(RowData[9]);
            msg.z = Float.parseFloat(RowData[10]);

            msg.autocontinue = Byte.parseByte(RowData[11]);

            msgMissionItems.add(msg);
		}

	}

	private static boolean isWaypointFile(BufferedReader reader)
			throws IOException {
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
