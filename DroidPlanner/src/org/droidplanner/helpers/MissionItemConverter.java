package org.droidplanner.helpers;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.json.JSONArray;
import org.json.JSONObject;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;

public class MissionItemConverter {
    static final String TAG = MissionItemConverter.class.getSimpleName();
    
    public static JSONObject populate(JSONObject jo, List<msg_mission_item> items) 
        throws Exception {
        
        if(jo == null) {
            jo = new JSONObject();
        }
        
        JSONArray a = new JSONArray();
        jo.put("items", a);
        
        final int size = items.size();
        for(int i = 0; i < size; ++i) {
            JSONObject joItem = populate(new JSONObject(), items.get(i));
            if(joItem != null) {
                a.put(i, joItem);
            }
        }
        
        return jo;
    }
    
    public static JSONObject populateMissionItems(JSONObject jo, List<MissionItem> items) throws Exception {
        JSONArray a = new JSONArray();
        final int size = items.size();
        for(int i = 0; i < size; ++i) {
            a.put(i, populate(new JSONObject(), items.get(i)));
        }
        
        jo.put("missionItems", a);
        return jo;
    }
    
    public static JSONObject populate(JSONObject jo, MissionItem item) throws Exception {
        
        jo.put("className", item.getClass().getName());
        List<msg_mission_item> mitems = item.packMissionItem();
        
        JSONArray a = new JSONArray();
        final int size = mitems.size();
        for(int i = 0; i < size; ++i) {
            a.put(i, populate(new JSONObject(), mitems.get(i)));
        }
        
        jo.put("packed_items", a);
        return jo;
    }

    public static JSONObject populate(JSONObject jo, msg_mission_item item) 
        throws Exception {
        
        if(jo == null) {
            jo = new JSONObject();
        }
        
        jo.put("param1", item.param1);
        jo.put("param2", item.param2);
        jo.put("param3", item.param3);
        jo.put("param4", item.param4);
        
        jo.put("x", item.x);
        jo.put("y", item.y);
        jo.put("z", item.z);
        
        jo.put("seq", item.seq);
        jo.put("command", item.command);
        jo.put("target_system", item.target_system);
        jo.put("target_component", item.target_component);
        
        jo.put("frame", item.frame);
        jo.put("current", item.current);
        jo.put("autocontinue", item.autocontinue);
        
        return jo;
    }
    
    public static List<MissionItem> populate(Mission mission, List<MissionItem> items, String str) throws Exception {
        if(items == null) {
            items = new ArrayList<MissionItem>();
        }
        
        JSONObject jo = new JSONObject(str);
        JSONArray a = jo.getJSONArray("missionItems");
        final int size = a.length();
        for(int i = 0; i < size; ++i) {
            JSONObject joItem = a.getJSONObject(i);
            String typeName = joItem.getString("className");
            MissionItem item = construct(typeName, mission);
            
            JSONArray pa = joItem.getJSONArray("packed_items");
            for(int j = 0; j < pa.length(); ++j) {
                JSONObject joP = pa.getJSONObject(j);
                msg_mission_item mmi = populate(new msg_mission_item(), joP);
                item.unpackMAVMessage(mmi);
            }
            
            items.add(item);
        }
        
        return items;
    }
    
    static MissionItem construct(String type, Mission mission) throws Exception {
        Class<?> t = Class.forName(type);
        Constructor<?> ctor = t.getConstructor(Mission.class);
        MissionItem item = (MissionItem)ctor.newInstance(mission);
        return item;
    }
    
    public static List<msg_mission_item> populatePacked(List<msg_mission_item> items, String str)
        throws Exception {
        
        if(items == null) {
            items = new ArrayList<msg_mission_item>();
        }
        
        JSONObject jo = new JSONObject(str);
        JSONArray a = jo.getJSONArray("items");
        
        final int size = a.length();
        for(int i = 0; i < size; ++i) {
            JSONObject joItem = a.getJSONObject(i);
            items.add(populate(new msg_mission_item(), joItem));
        }
        
        return items;
    }
    
    public static msg_mission_item populate(msg_mission_item item, JSONObject jo) 
        throws Exception {
        
        if(item == null) {
            item = new msg_mission_item();
        }
        
        item.param1 = (float)jo.optDouble("param1");
        item.param2 = (float)jo.optDouble("param2");
        item.param3 = (float)jo.optDouble("param3");
        
        item.x = (float)jo.optDouble("x");
        item.y = (float)jo.optDouble("y");
        item.z = (float)jo.optDouble("z");
        
        item.seq = (short)jo.optInt("seq");
        item.command = (short)jo.optInt("command");
        item.target_system = (byte)jo.optInt("target_system");
        item.target_component = (byte)jo.optInt("target_component");
        item.frame = (byte)jo.optInt("frame");
        item.current = (byte)jo.optInt("current");
        item.autocontinue = (byte)jo.optInt("autocontinue");
        
        return item;
    }
}
