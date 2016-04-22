package org.droidplanner.android.utils.tracker;

/**
 * Created by Aaron Licata on 2/17/2016.
 */

public interface TrackerUiInterface {
    public void setBox(float x, float y, float dx, float dy, boolean doInit);
    public void toggleCommands(float x, float y);
}
