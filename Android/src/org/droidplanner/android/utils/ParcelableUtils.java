package org.droidplanner.android.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Utilities functions for parcelable objects.
 */
public class ParcelableUtils {

    //Not instantiable
    private ParcelableUtils(){}

    /**
     * Marshall a parcelable object to a byte array
     * @param parceable
     * @return
     */
    public static byte[] marshall(Parcelable parceable) {
        Parcel parcel = Parcel.obtain();
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle(); // not sure if needed or a good idea
        return bytes;
    }

    /**
     * Unmarshall a parcel object from a byte array.
     * @param bytes
     * @return
     */
    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // this is extremely important!
        return parcel;
    }

    /**
     * Unmarshall a parcelable instance from a byte array.
     * @param bytes
     * @param creator
     * @param <T>
     * @return
     */
    public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshall(bytes);
        return creator.createFromParcel(parcel);
    }
}
