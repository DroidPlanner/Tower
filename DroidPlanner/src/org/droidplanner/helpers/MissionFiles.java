package org.droidplanner.helpers;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Locale;

import org.droidplanner.drone.variables.mission.Mission;

import android.os.Environment;

/** General mission-file saving utilities */
public class MissionFiles {
    static final String TAG = MissionFiles.class.getSimpleName();
    
    private static final File ROOT_DIR = new File(Environment.getExternalStorageDirectory(), "droidplanner");
    private static final File MISSION_DIR = new File(ROOT_DIR, "Missions");
    
    public static void save(Mission mission, String name) throws Exception {
        if(!name.endsWith(".mission")) {
            name += ".mission";
        }
        
        if(!MISSION_DIR.exists() && !MISSION_DIR.mkdirs()) {
            throw new Exception(String.format(Locale.getDefault(), "Unable to create directory %s", MISSION_DIR.toString()));
        }
        
        final File file = new File(MISSION_DIR, name);
        mission.saveToFile(file);
    }
    
    public static void load(Mission mission, String name) throws Exception {
        final File file = new File(MISSION_DIR, name + ".mission");
        if(file.exists()) {
            mission.loadFromFile(file);
        }
        else {
            throw new FileNotFoundException("File not found: " + file.toString());
        }
    }
    
    public static void delete(CharSequence[] names) {
        for(CharSequence name: names) {
            String str = name.toString() + ".mission";
            File f = new File(MISSION_DIR, str);
            if(f.exists()) {
                f.delete();
            }
        }
    }
    
    public static CharSequence[] getNames() {
        final ArrayList<CharSequence> list = new ArrayList<CharSequence>();
        
        File[] files = MISSION_DIR.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return (f.isFile() && f.getName().endsWith(".mission"));
            }
        });
        
        if(files != null) {
            for(File f: files) {
                list.add(f.getName().replaceAll(".mission", ""));
            }
        }
        
        return (CharSequence[])list.toArray(new CharSequence[list.size()]);
    }
}
