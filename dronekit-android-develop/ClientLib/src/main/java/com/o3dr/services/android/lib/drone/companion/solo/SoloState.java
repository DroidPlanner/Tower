package com.o3dr.services.android.lib.drone.companion.solo;

import android.os.Parcel;
import android.util.SparseArray;

import com.o3dr.android.client.utils.TxPowerComplianceCountries;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerMode.ControllerMode;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerUnits.ControllerUnit;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSetting;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageParser;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Stores state information for the sololink companion computer.
 * Created by Fredia Huya-Kouadio on 7/10/15.
 */
public class SoloState implements DroneAttribute {
    private String wifiSsid;
    private String wifiPassword;

    private String controllerVersion;
    private String controllerFirmwareVersion;

    private String vehicleVersion;
    private String autopilotVersion;
    private String gimbalVersion;

    private String txPowerCompliantCountry;

    private SparseArray<SoloButtonSetting> buttonSettings;

    @ControllerMode
    private int controllerMode;

    @ControllerUnit
    private String controllerUnit;

    public SoloState(){}

    public SoloState(String autopilotVersion, String controllerFirmwareVersion,
                     String controllerVersion, String vehicleVersion,
                     String wifiPassword, String wifiSsid, String txPowerCompliantCountry,
                     SparseArray<SoloButtonSetting> buttonSettings, String gimbalVersion,
                     @ControllerMode int controllerMode, @ControllerUnit String controllerUnit) {
        this.autopilotVersion = autopilotVersion;
        this.controllerFirmwareVersion = controllerFirmwareVersion;
        this.controllerVersion = controllerVersion;
        this.vehicleVersion = vehicleVersion;
        this.wifiPassword = wifiPassword;
        this.wifiSsid = wifiSsid;
        this.txPowerCompliantCountry = txPowerCompliantCountry;
        this.buttonSettings = buttonSettings;
        this.gimbalVersion = gimbalVersion;
        this.controllerMode = controllerMode;
        this.controllerUnit = controllerUnit;
    }

    public String getAutopilotVersion() {
        return autopilotVersion;
    }

    public String getControllerFirmwareVersion() {
        return controllerFirmwareVersion;
    }

    public String getControllerVersion() {
        return controllerVersion;
    }

    @ControllerMode
    public int getControllerMode(){
        return controllerMode;
    }

    public String getVehicleVersion() {
        return vehicleVersion;
    }

    public String getWifiPassword() {
        return wifiPassword;
    }

    public String getWifiSsid() {
        return wifiSsid;
    }

    private boolean isEUTxPowerCompliant() {
        return !TxPowerComplianceCountries.getDefaultCountry().name().equals(txPowerCompliantCountry);
    }

    public String getTxPowerCompliantCountry() {
        return txPowerCompliantCountry;
    }

    public SoloButtonSetting getButtonSetting(int buttonType){
        return buttonSettings.get(buttonType);
    }

    public String getGimbalVersion(){
        return gimbalVersion;
    }

    @ControllerUnit public String getControllerUnit(){
        return controllerUnit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.wifiSsid);
        dest.writeString(this.wifiPassword);
        dest.writeString(this.controllerVersion);
        dest.writeString(this.controllerFirmwareVersion);
        dest.writeString(this.vehicleVersion);
        dest.writeString(this.autopilotVersion);
        dest.writeByte(isEUTxPowerCompliant() ? (byte) 1 : (byte) 0);

        final int buttonCount = buttonSettings.size();
        dest.writeInt(buttonCount);

        for(int i = 0; i < buttonCount; i++){
            final SoloButtonSetting buttonSetting = buttonSettings.valueAt(i);
            if(buttonSetting == null){
                dest.writeInt(0);
                continue;
            }

            final byte[] buttonData = buttonSetting.toBytes();
            dest.writeInt(buttonData.length);
            dest.writeByteArray(buttonData);
        }

        dest.writeString(this.gimbalVersion);
        dest.writeInt(this.controllerMode);
        dest.writeString(this.controllerUnit);
        dest.writeString(txPowerCompliantCountry);
    }

    protected SoloState(Parcel in) {
        this.wifiSsid = in.readString();
        this.wifiPassword = in.readString();
        this.controllerVersion = in.readString();
        this.controllerFirmwareVersion = in.readString();
        this.vehicleVersion = in.readString();
        this.autopilotVersion = in.readString();
        //Throw away byte that was added to ensure backwards compatibility
        in.readByte();

        final int buttonCount = in.readInt();

        this.buttonSettings = new SparseArray<>(buttonCount);
        for(int i = 0; i < buttonCount; i++){
            final int dataSize = in.readInt();
            if(dataSize == 0)
                continue;

            final ByteBuffer dataBuffer = ByteBuffer.allocate(dataSize);
            in.readByteArray(dataBuffer.array());

            final List<TLVPacket> buttonsList = TLVMessageParser.parseTLVPacket(dataBuffer);
            if(!buttonsList.isEmpty()){
                for(TLVPacket tlvPacket : buttonsList){
                    if(tlvPacket instanceof SoloButtonSetting) {
                        final SoloButtonSetting button = (SoloButtonSetting) tlvPacket;
                        buttonSettings.put(button.getButton(), button);
                    }
                }
            }
        }

        this.gimbalVersion = in.readString();

        @ControllerMode final int tempMode = in.readInt();
        this.controllerMode = tempMode;

        @ControllerUnit final String tempUnit = in.readString();
        this.controllerUnit = tempUnit;
        this.txPowerCompliantCountry = in.readString();
    }

    public static final Creator<SoloState> CREATOR = new Creator<SoloState>() {
        public SoloState createFromParcel(Parcel source) {
            return new SoloState(source);
        }

        public SoloState[] newArray(int size) {
            return new SoloState[size];
        }
    };
}
