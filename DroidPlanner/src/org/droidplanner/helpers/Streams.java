package org.droidplanner.helpers;

import java.io.InputStream;
import java.io.OutputStream;

public class Streams {

    public static <T extends OutputStream> T copy(InputStream in, T out) throws Exception {
        byte[] buf = new byte[8192];
        
        for(int read = in.read(buf); read != -1; read = in.read(buf)) {
            out.write(buf, 0, read);
        }
        
        return out;
    }
    
    public static <T extends OutputStream> T copyAndClose(InputStream in, T out) throws Exception {
        try {
            copy(in, out);
        }
        finally {
            out.flush();
            out.close();
            in.close();
        }
        
        return out;
    }
}
