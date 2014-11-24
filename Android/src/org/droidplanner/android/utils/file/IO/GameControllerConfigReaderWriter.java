package org.droidplanner.android.utils.file.IO;

import android.content.Context;
import android.content.res.AssetManager;

import org.droidplanner.android.utils.file.DirectoryPath;
import org.droidplanner.android.utils.file.FileManager;
import org.droidplanner.android.utils.rc.input.GameController.GameControllerMappingParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class GameControllerConfigReaderWriter {
    private final String GCCONFIGFILENAME = "controllerconfig.json";

    public GameControllerMappingParser load(Context context) {
        try {
            return internalLoad(context);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private GameControllerMappingParser internalLoad(Context context) throws IOException {
        File dir = new File(DirectoryPath.getParametersPath());
        if (!dir.exists())
            dir.mkdirs();

        File jsonFile = new File(DirectoryPath.getParametersPath() + GCCONFIGFILENAME);
        InputStream in;
        if (!jsonFile.exists() || !FileManager.isExternalStorageAvailable()) {
            AssetManager assetManager = context.getAssets();
            in = assetManager.open(GCCONFIGFILENAME);
            OutputStream out = new FileOutputStream(jsonFile);
            FileManager.copyFile(in, out);
            in = assetManager.open(GCCONFIGFILENAME);
            out.close();
        }
        else
        {
            in = new FileInputStream(jsonFile);
        }
        byte[] configurationBytes = new byte[in.available()];
        in.read(configurationBytes);
        in.close();
        String json = new String(configurationBytes);

        GameControllerMappingParser mapping = new GameControllerMappingParser();
        mapping.parse(json);

        return mapping;
    }

    public void save(String json) {
        File jsonFile = new File(DirectoryPath.getParametersPath() + GCCONFIGFILENAME);
        if(!FileManager.isExternalStorageAvailable())
            return;

        PrintWriter out = null;
        try {
            out = new PrintWriter(jsonFile);
            out.write(json);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null)
                out.close();
        }
    }
}
