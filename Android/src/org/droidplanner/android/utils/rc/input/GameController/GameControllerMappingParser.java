package org.droidplanner.android.utils.rc.input.GameController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class GameControllerMappingParser { // Source:
                                           // https://github.com/ouya/docs/blob/master/ouya-everywhere-unity/ouya-everywhere-unity.md
                                           // starter
                                           // package/virtualcontrollerwithnativeapp

    public Controller mappingController = null;

    public class SingleAxisRemap {
        public int mSourceAxis = -1;
        public int mDestination = -1;
        public int mMultiplierReversed = 1;
    }

    public class DoubleAxisRemap {
        public int mIncrementAxis = -1;
        public int mDecrementAxis = -1;
        public int mDestination = -1;
    }

    public class Controller {
        public List<SingleAxisRemap> mSingleAxisRemap = new ArrayList<SingleAxisRemap>();
        public List<DoubleAxisRemap> mDoubleAxisRemap = new ArrayList<DoubleAxisRemap>();

        public List<SingleAxisRemap> getAxisRemap() {
            return mSingleAxisRemap;
        }

        public List<DoubleAxisRemap> getDoubleAxisRemap() {
            return mDoubleAxisRemap;
        }

        public SingleAxisRemap getSingleAxisRemap(int destination) {
            for (int x = 0; x < mSingleAxisRemap.size(); x++) {
                if (mSingleAxisRemap.get(x).mDestination == destination)
                    return mSingleAxisRemap.get(x);
            }
            return null;
        }

        public SingleAxisRemap getOrCreateSingleAxisRemap(int destination) {
            SingleAxisRemap temp = getSingleAxisRemap(destination);
            if (temp == null) {
                temp = new SingleAxisRemap();
                temp.mDestination = destination;
                mSingleAxisRemap.add(temp);
            }
            return temp;
        }

        public DoubleAxisRemap getDoubleAxisRemap(int destination) {
            for (int x = 0; x < mDoubleAxisRemap.size(); x++) {
                if (mDoubleAxisRemap.get(x).mDestination == destination)
                    return mDoubleAxisRemap.get(x);
            }
            return null;
        }

        public DoubleAxisRemap getOrCreateDoubleAxisRemap(int destination) {
            DoubleAxisRemap temp = getDoubleAxisRemap(destination);
            if (temp == null) {
                temp = new DoubleAxisRemap();
                temp.mDestination = destination;
                mDoubleAxisRemap.add(temp);
            }
            return temp;
        }

        public void removeSingleKeyRemap(int destination) {
            mSingleAxisRemap.remove(getSingleAxisRemap(destination));
        }

        public void removeDoubleKeyRemap(int destination) {
            mDoubleAxisRemap.remove(getDoubleAxisRemap(destination));
        }
    }

    public void parse(String jsonData) {
        Gson gson = new Gson();
        mappingController = gson.fromJson(jsonData, Controller.class);
        if(mappingController == null)
            mappingController = new Controller();
        if(mappingController.mSingleAxisRemap == null)
            mappingController.mSingleAxisRemap = new ArrayList<SingleAxisRemap>();
        if(mappingController.mDoubleAxisRemap == null)
            mappingController.mDoubleAxisRemap = new ArrayList<DoubleAxisRemap>();
    }

    public String getJSON() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(mappingController).toString();
    }
}
