package org.droidplanner.core.mission.survey;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.PolygonTools;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItemType;

import com.MAVLink.common.msg_mission_item;

public class Survey3D extends Survey {

	public Survey3D(Mission mission, List<Coord2D> points) {
		super(mission, points);
	}

	public List<Coord2D> getPath() {
		try {
			List<Coord2D> path = PolygonTools.offsetPolygon(polygon, 10).getPoints();
			return path;
		} catch (Exception e) {
			return new ArrayList<Coord2D>();
		}
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = new ArrayList<msg_mission_item>();
		for (Coord2D point: getPath()) {
			list.add(packSurveyPoint(point, surveyData.getAltitude()));			
		}
		return list;
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.SURVEY3D;
	}

	@Override
	public void build() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
