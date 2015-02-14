package org.droidplanner.android.utils.file.IO;

import android.content.Context;
import org.droidplanner.android.utils.file.DirectoryPath;
import org.droidplanner.android.utils.file.FileManager;
import org.droidplanner.android.utils.file.FileStream;
import org.droidplanner.android.utils.rc.input.GameController.Controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GameControllerConfigReaderWriter {
    private final static String GCCONFIGFILENAME = "controller.conf";

    public Controller load(Context context) {
        try {
            ObjectInputStream inReader = new ObjectInputStream(
                    FileStream.getControllerConfigStream(context, GCCONFIGFILENAME));
            return (Controller) inReader.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(Controller controller) {
        File jsonFile = new File(DirectoryPath.getParametersPath() + GCCONFIGFILENAME);
        if (!FileManager.isExternalStorageAvailable())
            return false;

        try {
            ObjectOutputStream out = null;
            out = new ObjectOutputStream(new FileOutputStream(jsonFile));
            out.writeObject(controller);
            if (out != null)
                out.close();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
