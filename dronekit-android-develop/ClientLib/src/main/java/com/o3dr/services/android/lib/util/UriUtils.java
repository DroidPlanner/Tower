package com.o3dr.services.android.lib.util;

import android.content.Context;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by fhuya on 8/13/2016.
 */
public class UriUtils {

    private UriUtils() {
    }

    /**
     * Retrieves an output stream from the given uri.
     *
     * @param uri
     * @return
     * @since 3.0.0
     */
    public static OutputStream getOutputStream(Context context, Uri uri) throws IOException {
        return context.getContentResolver().openOutputStream(uri);
    }

    /**
     * Retrieves an input stream from the given uri.
     *
     * @param uri
     * @return
     * @since 3.0.0
     */
    public static InputStream getInputStream(Context context,  Uri uri) throws IOException {
        return context.getContentResolver().openInputStream(uri);
    }
}
