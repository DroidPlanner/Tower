package com.o3dr.services.android.lib.util;

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
     * @param parcelable
     * @return
     */
    public static byte[] marshall(Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle(); // not sure if needed or a good idea
        return bytes;
    }

    /**
     * Unmarshall a parcel object from a byte array.
     * @param bytes
     * @return
     */
    private static Parcel unmarshall(byte[] bytes) {
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
        T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }

    /**
     * Unmarshall a parcel object from a byte array.
     * @param bytes
     * @return
     */
    private static Parcel unmarshall(byte[] bytes, int offset, int length) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, offset, length);
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
    public static <T> T unmarshall(byte[] bytes, int offset, int length, Parcelable.Creator<T>
            creator) {
        Parcel parcel = unmarshall(bytes, offset, length);
        T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }
}
