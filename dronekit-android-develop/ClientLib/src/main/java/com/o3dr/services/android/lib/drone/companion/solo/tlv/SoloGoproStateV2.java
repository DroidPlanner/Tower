package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.property.DroneAttribute;

import java.nio.ByteBuffer;

/**
 * Encapsulate all state that an app might want to know about the GoPro in one packet.
 */
public class SoloGoproStateV2 extends TLVPacket implements DroneAttribute {

    public static final int MESSAGE_LENGTH = 36;

    /**
     * Version of the spec.
     */
    private byte version;

    /**
     * Model of the camera
     */
    private byte model;

    /**
     * Gopro status
     */
    private byte status;

    /**
     * Camera recording status
     */
    private byte recording;

    /**
     * Gopro capture mode.
     */
    private byte captureMode;

    private byte ntsc_pal;

    /**
     * Camera field of view.
     */
    private byte fov;

    /**
     * Camera video resolution;
     */
    private byte videoResolution;

    private byte fps;

    private byte lowLight;

    private byte photoResolution;

    private byte photoBurstRate;

    /**
     * Requires compatible video settings
     */
    private byte videoProtune;

    /**
     * Hero 3 only. Requires protune on
     */
    private byte videoWhiteBalance;

    /**
     * Hero 3 only. Requires protune on
     */
    private byte videoColor;

    /**
     * Hero 3 only. Requires protune on
     */
    private byte videoGain;

    /**
     * Hero 3 only. Requires protune on
     */
    private byte videoSharpness;

    /**
     * Requires protune on. Can be set on Hero4 but currently does not return correct setting.
     */
    private byte videoExposure;

    private byte gimbalEnabled;

    //EXTRA FIELDS
    private byte extra1;
    private byte extra2;
    private byte extra3;
    private byte extra4;
    private byte extra5;
    private byte extra6;
    private byte extra7;

    private short extra8;
    private short extra9;
    private short extra10;
    private short extra11;
    private short extra12;

    public SoloGoproStateV2(ByteBuffer packetBuffer) {
        super(TLVMessageTypes.TYPE_SOLO_GOPRO_STATE_V2, MESSAGE_LENGTH);

        this.version = packetBuffer.get();
        this.model = packetBuffer.get();
        this.status = packetBuffer.get();
        this.recording = packetBuffer.get();
        this.captureMode = packetBuffer.get();
        this.ntsc_pal = packetBuffer.get();
        this.videoResolution = packetBuffer.get();
        this.fps = packetBuffer.get();
        this.fov = packetBuffer.get();
        this.lowLight = packetBuffer.get();
        this.photoResolution = packetBuffer.get();
        this.photoBurstRate = packetBuffer.get();
        this.videoProtune = packetBuffer.get();
        this.videoWhiteBalance = packetBuffer.get();
        this.videoColor = packetBuffer.get();
        this.videoGain = packetBuffer.get();
        this.videoSharpness = packetBuffer.get();
        this.videoExposure = packetBuffer.get();
        this.gimbalEnabled = packetBuffer.get();

        //Instantiate the extras
        this.extra1 = packetBuffer.get();
        this.extra2 = packetBuffer.get();
        this.extra3 = packetBuffer.get();
        this.extra4 = packetBuffer.get();
        this.extra5 = packetBuffer.get();
        this.extra6 = packetBuffer.get();
        this.extra7 = packetBuffer.get();

        this.extra8 = packetBuffer.getShort();
        this.extra9 = packetBuffer.getShort();
        this.extra10 = packetBuffer.getShort();
        this.extra11 = packetBuffer.getShort();
        this.extra12 = packetBuffer.getShort();
    }


    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.put(version);
        valueCarrier.put(model);
        valueCarrier.put(status);
        valueCarrier.put(recording);
        valueCarrier.put(captureMode);
        valueCarrier.put(ntsc_pal);
        valueCarrier.put(videoResolution);
        valueCarrier.put(fps);
        valueCarrier.put(fov);
        valueCarrier.put(lowLight);
        valueCarrier.put(photoResolution);
        valueCarrier.put(photoBurstRate);
        valueCarrier.put(videoProtune);
        valueCarrier.put(videoWhiteBalance);
        valueCarrier.put(videoColor);
        valueCarrier.put(videoGain);
        valueCarrier.put(videoSharpness);
        valueCarrier.put(videoExposure);
        valueCarrier.put(gimbalEnabled);

        valueCarrier.put(extra1);
        valueCarrier.put(extra2);
        valueCarrier.put(extra3);
        valueCarrier.put(extra4);
        valueCarrier.put(extra5);
        valueCarrier.put(extra6);
        valueCarrier.put(extra7);

        valueCarrier.putShort(extra8);
        valueCarrier.putShort(extra9);
        valueCarrier.putShort(extra10);
        valueCarrier.putShort(extra11);
        valueCarrier.putShort(extra12);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(this.version);
        dest.writeByte(this.model);
        dest.writeByte(this.status);
        dest.writeByte(this.recording);
        dest.writeByte(this.captureMode);
        dest.writeByte(this.ntsc_pal);
        dest.writeByte(this.fov);
        dest.writeByte(this.videoResolution);
        dest.writeByte(this.fps);
        dest.writeByte(this.lowLight);
        dest.writeByte(this.photoResolution);
        dest.writeByte(this.photoBurstRate);
        dest.writeByte(this.videoProtune);
        dest.writeByte(this.videoWhiteBalance);
        dest.writeByte(this.videoColor);
        dest.writeByte(this.videoGain);
        dest.writeByte(this.videoSharpness);
        dest.writeByte(this.videoExposure);
        dest.writeByte(this.gimbalEnabled);
        dest.writeByte(this.extra1);
        dest.writeByte(this.extra2);
        dest.writeByte(this.extra3);
        dest.writeByte(this.extra4);
        dest.writeByte(this.extra5);
        dest.writeByte(this.extra6);
        dest.writeByte(this.extra7);
        dest.writeInt(this.extra8);
        dest.writeInt(this.extra9);
        dest.writeInt(this.extra10);
        dest.writeInt(this.extra11);
        dest.writeInt(this.extra12);
    }

    protected SoloGoproStateV2(Parcel in) {
        super(in);
        this.version = in.readByte();
        this.model = in.readByte();
        this.status = in.readByte();
        this.recording = in.readByte();
        this.captureMode = in.readByte();
        this.ntsc_pal = in.readByte();
        this.fov = in.readByte();
        this.videoResolution = in.readByte();
        this.fps = in.readByte();
        this.lowLight = in.readByte();
        this.photoResolution = in.readByte();
        this.photoBurstRate = in.readByte();
        this.videoProtune = in.readByte();
        this.videoWhiteBalance = in.readByte();
        this.videoColor = in.readByte();
        this.videoGain = in.readByte();
        this.videoSharpness = in.readByte();
        this.videoExposure = in.readByte();
        this.gimbalEnabled = in.readByte();
        this.extra1 = in.readByte();
        this.extra2 = in.readByte();
        this.extra3 = in.readByte();
        this.extra4 = in.readByte();
        this.extra5 = in.readByte();
        this.extra6 = in.readByte();
        this.extra7 = in.readByte();
        this.extra8 = (short) in.readInt();
        this.extra9 = (short) in.readInt();
        this.extra10 = (short) in.readInt();
        this.extra11 = (short) in.readInt();
        this.extra12 = (short) in.readInt();
    }

    public static final Creator<SoloGoproStateV2> CREATOR = new Creator<SoloGoproStateV2>() {
        public SoloGoproStateV2 createFromParcel(Parcel source) {
            return new SoloGoproStateV2(source);
        }

        public SoloGoproStateV2[] newArray(int size) {
            return new SoloGoproStateV2[size];
        }
    };

    @Override
    public String toString() {
        return "SoloGoproStateV2{" +
                "captureMode=" + captureMode +
                ", version=" + version +
                ", model=" + model +
                ", status=" + status +
                ", recording=" + recording +
                ", ntsc_pal=" + ntsc_pal +
                ", fov=" + fov +
                ", videoResolution=" + videoResolution +
                ", fps=" + fps +
                ", lowLight=" + lowLight +
                ", photoResolution=" + photoResolution +
                ", photoBurstRate=" + photoBurstRate +
                ", videoProtune=" + videoProtune +
                ", videoWhiteBalance=" + videoWhiteBalance +
                ", videoColor=" + videoColor +
                ", videoGain=" + videoGain +
                ", videoSharpness=" + videoSharpness +
                ", videoExposure=" + videoExposure +
                ", gimbalEnabled=" + gimbalEnabled +
                ", extra1=" + extra1 +
                ", extra2=" + extra2 +
                ", extra3=" + extra3 +
                ", extra4=" + extra4 +
                ", extra5=" + extra5 +
                ", extra6=" + extra6 +
                ", extra7=" + extra7 +
                ", extra8=" + extra8 +
                ", extra9=" + extra9 +
                ", extra10=" + extra10 +
                ", extra11=" + extra11 +
                ", extra12=" + extra12 +
                '}';
    }

    public byte getFov() {
        return fov;
    }

    public byte getFps() {
        return fps;
    }

    public byte getGimbalEnabled() {
        return gimbalEnabled;
    }

    public byte getLowLight() {
        return lowLight;
    }

    public byte getModel() {
        return model;
    }

    public byte getNtsc_pal() {
        return ntsc_pal;
    }

    public byte getPhotoBurstRate() {
        return photoBurstRate;
    }

    public byte getPhotoResolution() {
        return photoResolution;
    }

    public byte getRecording() {
        return recording;
    }

    public byte getStatus() {
        return status;
    }

    public byte getVersion() {
        return version;
    }

    public byte getVideoColor() {
        return videoColor;
    }

    public byte getVideoExposure() {
        return videoExposure;
    }

    public byte getVideoGain() {
        return videoGain;
    }

    public byte getVideoProtune() {
        return videoProtune;
    }

    public byte getVideoResolution() {
        return videoResolution;
    }

    public byte getVideoSharpness() {
        return videoSharpness;
    }

    public byte getVideoWhiteBalance() {
        return videoWhiteBalance;
    }

    public byte getCaptureMode() {
        return captureMode;
    }
}
