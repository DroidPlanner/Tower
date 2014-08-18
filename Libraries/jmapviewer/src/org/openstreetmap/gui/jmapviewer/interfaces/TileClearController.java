// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.interfaces;

import java.io.File;
    
public interface TileClearController {

    void initClearDir(File dir);

    void initClearFiles(File[] files);

    boolean cancel();

    void fileDeleted(File file);

    void clearFinished();
}