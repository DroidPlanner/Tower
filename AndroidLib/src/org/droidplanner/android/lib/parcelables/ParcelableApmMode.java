package org.droidplanner.android.lib.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

import com.MAVLink.Messages.ApmModes;

/**
 * Parcelable wrapper for an ApmMode object.
 */
public class ParcelableApmMode implements Parcelable {

    private final ApmModes mApmMode;

    public ParcelableApmMode(ApmModes apmMode) {
        mApmMode = apmMode;
    }

    public ApmModes getApmMode(){
        return mApmMode;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mApmMode.getNumber());
        dest.writeInt(mApmMode.getType());
    }

    private ParcelableApmMode(Parcel in) {
        int number = in.readInt();
        int type = in.readInt();
        mApmMode = ApmModes.getMode(number, type);
    }

    public static final Creator<ParcelableApmMode> CREATOR = new Creator<ParcelableApmMode>() {
        public ParcelableApmMode createFromParcel(Parcel source) {return new ParcelableApmMode(source);}

        public ParcelableApmMode[] newArray(int size) {return new ParcelableApmMode[size];}
    };
}
