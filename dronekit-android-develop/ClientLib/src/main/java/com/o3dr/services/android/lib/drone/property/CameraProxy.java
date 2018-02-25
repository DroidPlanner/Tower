package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 11/30/14.
 */
public class CameraProxy implements DroneAttribute {

    private CameraDetail cameraDetail;
    private List<FootPrint> footPrints = new ArrayList<FootPrint>();
    private FootPrint currentFieldOfView;
    private List<CameraDetail> availableCameraInfos = new ArrayList<CameraDetail>();

    public CameraProxy(CameraDetail cameraDetail, FootPrint currentFieldOfView,
                       List<FootPrint> footPrints, List<CameraDetail> availableCameraInfos){
        this.cameraDetail = cameraDetail;
        this.currentFieldOfView = currentFieldOfView;
        this.footPrints = footPrints;
        this.availableCameraInfos = availableCameraInfos;
    }

    public CameraDetail getCameraDetail() {
        return cameraDetail;
    }

    public List<FootPrint> getFootPrints() {
        return footPrints;
    }

    public FootPrint getLastFootPrint(){
        return footPrints.get(footPrints.size() -1);
    }

    public FootPrint getCurrentFieldOfView() {
        return currentFieldOfView;
    }

    public List<CameraDetail> getAvailableCameraInfos() {
        return availableCameraInfos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.cameraDetail, 0);
        dest.writeTypedList(footPrints);
        dest.writeParcelable(this.currentFieldOfView, 0);
        dest.writeTypedList(availableCameraInfos);
    }

    private CameraProxy(Parcel in) {
        this.cameraDetail = in.readParcelable(CameraDetail.class.getClassLoader());
        in.readTypedList(footPrints, FootPrint.CREATOR);
        this.currentFieldOfView = in.readParcelable(FootPrint.class.getClassLoader());
        in.readTypedList(availableCameraInfos, CameraDetail.CREATOR);
    }

    public static final Creator<CameraProxy> CREATOR = new Creator<CameraProxy>() {
        public CameraProxy createFromParcel(Parcel source) {
            return new CameraProxy(source);
        }

        public CameraProxy[] newArray(int size) {
            return new CameraProxy[size];
        }
    };
}
