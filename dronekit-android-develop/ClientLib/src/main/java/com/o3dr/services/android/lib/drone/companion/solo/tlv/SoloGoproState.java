package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.property.DroneAttribute;

import java.nio.ByteBuffer;

/**
 * Created by Fredia Huya-Kouadio on 7/28/15.
 */
public class SoloGoproState extends TLVPacket implements DroneAttribute {

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

    /**
     * Camera field of view.
     */
    private byte fov;

    /**
     * Camera video resolution;
     */
    private byte videoResolution;

    private byte fps;

    private byte whiteBalance;

    private byte proTune;

    private byte videoExposure;

    private byte photoResolution;

    private byte photoExposure;

    private byte ntsc_pal;

    private byte lowLight;

    private byte spotMeter;

    private byte batteryRemaining;

    private byte photoRemaining;

    private byte photoTaken;

    private byte videoRemaining;

    private byte videoTaken;

    private byte color;

    private byte sharpness;

    private byte burstShutterRate;

    private byte continuousShutterSpeed;

    private byte timeLapseInterval;

    private short extra1;

    private short extra2;

    private short extra3;

    private short extra4;

    private short extra5;

    public SoloGoproState(byte batteryRemaining, byte burstShutterRate, byte captureMode, byte color,
                          byte continuousShutterSpeed, short extra1, short extra2, short extra3,
                          short extra4, short extra5, byte fov, byte fps, byte lowLight, byte model,
                          byte ntsc_pal, byte photoExposure, byte photoRemaining, byte photoResolution,
                          byte photoTaken, byte proTune, byte recording, byte sharpness,
                          byte spotMeter, byte status, byte timeLapseInterval, byte version,
                          byte videoExposure, byte videoRemaining, byte videoResolution,
                          byte videoTaken, byte whiteBalance) {
        super(TLVMessageTypes.TYPE_SOLO_GOPRO_STATE, MESSAGE_LENGTH);

        this.batteryRemaining = batteryRemaining;
        this.burstShutterRate = burstShutterRate;
        this.captureMode = captureMode;
        this.color = color;
        this.continuousShutterSpeed = continuousShutterSpeed;
        this.extra1 = extra1;
        this.extra2 = extra2;
        this.extra3 = extra3;
        this.extra4 = extra4;
        this.extra5 = extra5;
        this.fov = fov;
        this.fps = fps;
        this.lowLight = lowLight;
        this.model = model;
        this.ntsc_pal = ntsc_pal;
        this.photoExposure = photoExposure;
        this.photoRemaining = photoRemaining;
        this.photoResolution = photoResolution;
        this.photoTaken = photoTaken;
        this.proTune = proTune;
        this.recording = recording;
        this.sharpness = sharpness;
        this.spotMeter = spotMeter;
        this.status = status;
        this.timeLapseInterval = timeLapseInterval;
        this.version = version;
        this.videoExposure = videoExposure;
        this.videoRemaining = videoRemaining;
        this.videoResolution = videoResolution;
        this.videoTaken = videoTaken;
        this.whiteBalance = whiteBalance;
    }

    public SoloGoproState(ByteBuffer packetBuffer){
        super(TLVMessageTypes.TYPE_SOLO_GOPRO_STATE, MESSAGE_LENGTH);

        this.version = packetBuffer.get();
        this.model = packetBuffer.get();
        this.status = packetBuffer.get();
        this.recording = packetBuffer.get();
        this.captureMode = packetBuffer.get();
        this.fov = packetBuffer.get();
        this.videoResolution = packetBuffer.get();
        this.fps = packetBuffer.get();
        this.whiteBalance = packetBuffer.get();
        this.proTune = packetBuffer.get();
        this.videoExposure = packetBuffer.get();
        this.photoResolution = packetBuffer.get();
        this.photoExposure = packetBuffer.get();
        this.ntsc_pal = packetBuffer.get();
        this.lowLight = packetBuffer.get();
        this.spotMeter = packetBuffer.get();
        this.batteryRemaining = packetBuffer.get();
        this.photoRemaining = packetBuffer.get();
        this.photoTaken = packetBuffer.get();
        this.videoRemaining = packetBuffer.get();
        this.videoTaken = packetBuffer.get();
        this.color = packetBuffer.get();
        this.sharpness = packetBuffer.get();
        this.burstShutterRate = packetBuffer.get();
        this.continuousShutterSpeed = packetBuffer.get();
        this.timeLapseInterval = packetBuffer.get();

        this.extra1 = packetBuffer.getShort();
        this.extra2 = packetBuffer.getShort();
        this.extra3 = packetBuffer.getShort();
        this.extra4 = packetBuffer.getShort();
        this.extra5 = packetBuffer.getShort();
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier) {
        valueCarrier.put(version);
        valueCarrier.put(model);
        valueCarrier.put(status);
        valueCarrier.put(recording);
        valueCarrier.put(captureMode);
        valueCarrier.put(fov);
        valueCarrier.put(videoResolution);
        valueCarrier.put(fps);
        valueCarrier.put(whiteBalance);
        valueCarrier.put(proTune);
        valueCarrier.put(videoExposure);
        valueCarrier.put(photoResolution);
        valueCarrier.put(photoExposure);
        valueCarrier.put(ntsc_pal);
        valueCarrier.put(lowLight);
        valueCarrier.put(spotMeter);
        valueCarrier.put(batteryRemaining);
        valueCarrier.put(photoRemaining);
        valueCarrier.put(photoTaken);
        valueCarrier.put(videoRemaining);
        valueCarrier.put(videoTaken);
        valueCarrier.put(color);
        valueCarrier.put(sharpness);
        valueCarrier.put(burstShutterRate);
        valueCarrier.put(continuousShutterSpeed);
        valueCarrier.put(timeLapseInterval);

        valueCarrier.putShort(extra1);
        valueCarrier.putShort(extra2);
        valueCarrier.putShort(extra3);
        valueCarrier.putShort(extra4);
        valueCarrier.putShort(extra5);
    }

    public byte getBatteryRemaining() {
        return batteryRemaining;
    }

    public byte getBurstShutterRate() {
        return burstShutterRate;
    }

    public byte getCaptureMode() {
        return captureMode;
    }

    public byte getColor() {
        return color;
    }

    public byte getContinuousShutterSpeed() {
        return continuousShutterSpeed;
    }

    public short getExtra1() {
        return extra1;
    }

    public short getExtra2() {
        return extra2;
    }

    public short getExtra3() {
        return extra3;
    }

    public short getExtra4() {
        return extra4;
    }

    public short getExtra5() {
        return extra5;
    }

    public byte getFov() {
        return fov;
    }

    public byte getFps() {
        return fps;
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

    public byte getPhotoExposure() {
        return photoExposure;
    }

    public byte getPhotoRemaining() {
        return photoRemaining;
    }

    public byte getPhotoResolution() {
        return photoResolution;
    }

    public byte getPhotoTaken() {
        return photoTaken;
    }

    public byte getProTune() {
        return proTune;
    }

    public byte getRecording() {
        return recording;
    }

    public byte getSharpness() {
        return sharpness;
    }

    public byte getSpotMeter() {
        return spotMeter;
    }

    public byte getStatus() {
        return status;
    }

    public byte getTimeLapseInterval() {
        return timeLapseInterval;
    }

    public byte getVersion() {
        return version;
    }

    public byte getVideoExposure() {
        return videoExposure;
    }

    public byte getVideoRemaining() {
        return videoRemaining;
    }

    public byte getVideoResolution() {
        return videoResolution;
    }

    public byte getVideoTaken() {
        return videoTaken;
    }

    public byte getWhiteBalance() {
        return whiteBalance;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(this.version);
        dest.writeByte(this.model);
        dest.writeByte(this.status);
        dest.writeByte(this.recording);
        dest.writeByte(this.captureMode);
        dest.writeByte(this.fov);
        dest.writeByte(this.videoResolution);
        dest.writeByte(this.fps);
        dest.writeByte(this.whiteBalance);
        dest.writeByte(this.proTune);
        dest.writeByte(this.videoExposure);
        dest.writeByte(this.photoResolution);
        dest.writeByte(this.photoExposure);
        dest.writeByte(this.ntsc_pal);
        dest.writeByte(this.lowLight);
        dest.writeByte(this.spotMeter);
        dest.writeByte(this.batteryRemaining);
        dest.writeByte(this.photoRemaining);
        dest.writeByte(this.photoTaken);
        dest.writeByte(this.videoRemaining);
        dest.writeByte(this.videoTaken);
        dest.writeByte(this.color);
        dest.writeByte(this.sharpness);
        dest.writeByte(this.burstShutterRate);
        dest.writeByte(this.continuousShutterSpeed);
        dest.writeByte(this.timeLapseInterval);
        dest.writeInt(this.extra1);
        dest.writeInt(this.extra2);
        dest.writeInt(this.extra3);
        dest.writeInt(this.extra4);
        dest.writeInt(this.extra5);
    }

    protected SoloGoproState(Parcel in) {
        super(in);
        this.version = in.readByte();
        this.model = in.readByte();
        this.status = in.readByte();
        this.recording = in.readByte();
        this.captureMode = in.readByte();
        this.fov = in.readByte();
        this.videoResolution = in.readByte();
        this.fps = in.readByte();
        this.whiteBalance = in.readByte();
        this.proTune = in.readByte();
        this.videoExposure = in.readByte();
        this.photoResolution = in.readByte();
        this.photoExposure = in.readByte();
        this.ntsc_pal = in.readByte();
        this.lowLight = in.readByte();
        this.spotMeter = in.readByte();
        this.batteryRemaining = in.readByte();
        this.photoRemaining = in.readByte();
        this.photoTaken = in.readByte();
        this.videoRemaining = in.readByte();
        this.videoTaken = in.readByte();
        this.color = in.readByte();
        this.sharpness = in.readByte();
        this.burstShutterRate = in.readByte();
        this.continuousShutterSpeed = in.readByte();
        this.timeLapseInterval = in.readByte();
        this.extra1 = (short) in.readInt();
        this.extra2 = (short) in.readInt();
        this.extra3 = (short) in.readInt();
        this.extra4 = (short) in.readInt();
        this.extra5 = (short) in.readInt();
    }

    @Override
    public String toString() {
        return "SoloGoproState{" +
                "batteryRemaining=" + batteryRemaining +
                ", version=" + version +
                ", model=" + model +
                ", status=" + status +
                ", recording=" + recording +
                ", captureMode=" + captureMode +
                ", fov=" + fov +
                ", videoResolution=" + videoResolution +
                ", fps=" + fps +
                ", whiteBalance=" + whiteBalance +
                ", proTune=" + proTune +
                ", videoExposure=" + videoExposure +
                ", photoResolution=" + photoResolution +
                ", photoExposure=" + photoExposure +
                ", ntsc_pal=" + ntsc_pal +
                ", lowLight=" + lowLight +
                ", spotMeter=" + spotMeter +
                ", photoRemaining=" + photoRemaining +
                ", photoTaken=" + photoTaken +
                ", videoRemaining=" + videoRemaining +
                ", videoTaken=" + videoTaken +
                ", color=" + color +
                ", sharpness=" + sharpness +
                ", burstShutterRate=" + burstShutterRate +
                ", continuousShutterSpeed=" + continuousShutterSpeed +
                ", timeLapseInterval=" + timeLapseInterval +
                ", extra1=" + extra1 +
                ", extra2=" + extra2 +
                ", extra3=" + extra3 +
                ", extra4=" + extra4 +
                ", extra5=" + extra5 +
                '}';
    }

    public static final Creator<SoloGoproState> CREATOR = new Creator<SoloGoproState>() {
        public SoloGoproState createFromParcel(Parcel source) {
            return new SoloGoproState(source);
        }

        public SoloGoproState[] newArray(int size) {
            return new SoloGoproState[size];
        }
    };
}
