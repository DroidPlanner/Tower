package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.util.Log;

import com.o3dr.services.android.lib.drone.companion.solo.tlv.sitescan.inspect.SoloInspectStart;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.sitescan.inspect.SoloInspectSetWaypoint;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.sitescan.inspect.SoloInspectMoveGimbal;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.sitescan.inspect.SoloInspectMoveVehicle;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc.SoloSplineAttach;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc.SoloSplineDurations;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc.SoloSplinePathSettings;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc.SoloSplinePlay;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc.SoloSplinePlaybackStatus;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc.SoloSplinePoint;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc.SoloSplineRecord;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.mpcc.SoloSplineSeek;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.sitescan.scan.SoloScanStart;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.sitescan.survey.SoloSurveyStart;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_ARTOO_INPUT_REPORT_MESSAGE;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_RTL_HOME_POINT;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_CABLE_CAM_OPTIONS;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_CABLE_CAM_WAYPOINT;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_FOLLOW_OPTIONS;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_FOLLOW_OPTIONS_V2;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GET_BUTTON_SETTING;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GOPRO_RECORD;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GOPRO_REQUEST_STATE;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GOPRO_SET_EXTENDED_REQUEST;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GOPRO_SET_REQUEST;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GOPRO_STATE;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_GOPRO_STATE_V2;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_INSPECT_MOVE_GIMBAL;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_INSPECT_MOVE_VEHICLE;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_INSPECT_SET_WAYPOINT;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_INSPECT_START;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_MESSAGE_GET_CURRENT_SHOT;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_MESSAGE_LOCATION;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_MESSAGE_RECORD_POSITION;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_MESSAGE_SET_CURRENT_SHOT;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_MESSAGE_SHOT_MANAGER_ERROR;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_PANO_OPTIONS;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_PANO_STATUS;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_PAUSE_BUTTON;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_REWIND_OPTIONS;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SCAN_START;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SET_BUTTON_SETTING;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SHOT_ERROR;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SHOT_OPTIONS;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SPLINE_ATTACH;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SPLINE_PATH_SETTINGS;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SPLINE_DURATIONS;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SPLINE_PLAY;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SPLINE_PLAYBACK_STATUS;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SPLINE_POINT;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SPLINE_RECORD;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SPLINE_SEEK;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_SURVEY_START;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_ZIPLINE_LOCK;
import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.TYPE_SOLO_ZIPLINE_OPTIONS;

/**
 * Utility class to generate tlv packet from received bytes.
 */
public class TLVMessageParser {

    private static final String TAG = TLVMessageParser.class.getSimpleName();

    public static List<TLVPacket> parseTLVPacket(byte[] packetData){
        if(packetData == null || packetData.length == 0)
            return null;

        return parseTLVPacket(ByteBuffer.wrap(packetData));
    }

    public static List<TLVPacket> parseTLVPacket(ByteBuffer packetBuffer) {
        final List<TLVPacket> packetList = new ArrayList<>();

        if (packetBuffer == null)
            return packetList;

        final int bufferSize = packetBuffer.limit();
        if(bufferSize <= 0)
            return packetList;

        final ByteOrder originalOrder = packetBuffer.order();
        packetBuffer.order(TLVPacket.TLV_BYTE_ORDER);

        int messageType = -1;
        try {

            while (packetBuffer.remaining() >= TLVPacket.MIN_TLV_PACKET_SIZE) {
                messageType = packetBuffer.getInt();
                final int messageLength = packetBuffer.getInt();

                int remaining = packetBuffer.remaining();
                Log.d(TAG, String.format("Received message %d of with value of length %d. Remaining buffer size is %d", messageType, messageLength, remaining));

                if (messageLength > remaining) {
                    break;
                }

                TLVPacket packet = null;
                packetBuffer.mark();

                switch (messageType) {
                    case TYPE_SOLO_MESSAGE_GET_CURRENT_SHOT:
                    case TYPE_SOLO_MESSAGE_SET_CURRENT_SHOT: {
                        final int shotType = packetBuffer.getInt();
                        if (messageType == TYPE_SOLO_MESSAGE_GET_CURRENT_SHOT)
                            packet = new SoloMessageShotGetter(shotType);
                        else
                            packet = new SoloMessageShotSetter(shotType);
                        break;
                    }

                    case TYPE_SOLO_MESSAGE_LOCATION: {
                        final double latitude = packetBuffer.getDouble();
                        final double longitude = packetBuffer.getDouble();
                        final float altitude = packetBuffer.getFloat();
                        packet = new SoloMessageLocation(latitude, longitude, altitude);
                        break;
                    }

                    case TYPE_SOLO_MESSAGE_RECORD_POSITION: {
                        packet = new SoloMessageRecordPosition();
                        break;
                    }

                    case TYPE_SOLO_CABLE_CAM_OPTIONS: {
                        final short camInterpolation = packetBuffer.getShort();
                        final short yawDirectionClockwise = packetBuffer.getShort();
                        final float cruiseSpeed = packetBuffer.getFloat();
                        packet = new SoloCableCamOptions(camInterpolation, yawDirectionClockwise, cruiseSpeed);
                        break;
                    }

                    case TYPE_SOLO_GET_BUTTON_SETTING:
                    case TYPE_SOLO_SET_BUTTON_SETTING: {
                        final int button = packetBuffer.getInt();
                        final int event = packetBuffer.getInt();
                        final int shotType = packetBuffer.getInt();
                        final int flightMode = packetBuffer.getInt();
                        if (messageType == TYPE_SOLO_GET_BUTTON_SETTING)
                            packet = new SoloButtonSettingGetter(button, event, shotType, flightMode);
                        else
                            packet = new SoloButtonSettingSetter(button, event, shotType, flightMode);
                        break;
                    }

                    case TYPE_SOLO_FOLLOW_OPTIONS: {
                        packet = new SoloFollowOptions(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_FOLLOW_OPTIONS_V2: {
                        packet = new SoloFollowOptionsV2(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_SHOT_OPTIONS: {
                        final float cruiseSpeed = packetBuffer.getFloat();
                        packet = new SoloShotOptions(cruiseSpeed);
                        break;
                    }

                    case TYPE_SOLO_SHOT_ERROR: {
                        final int errorType = packetBuffer.getInt();
                        packet = new SoloShotError(errorType);
                        break;
                    }

                    case TYPE_SOLO_MESSAGE_SHOT_MANAGER_ERROR: {
                        final byte[] exceptionData = new byte[messageLength];
                        packetBuffer.get(exceptionData);
                        packet = new SoloMessageShotManagerError(new String(exceptionData));
                        break;
                    }

                    case TYPE_SOLO_CABLE_CAM_WAYPOINT: {
                        final double latitude = packetBuffer.getDouble();
                        final double longitude = packetBuffer.getDouble();
                        final float altitude = packetBuffer.getFloat();
                        final float degreesYaw = packetBuffer.getFloat();
                        final float pitch = packetBuffer.getFloat();

                        packet = new SoloCableCamWaypoint(latitude, longitude, altitude, degreesYaw, pitch);
                        break;
                    }

                    case TYPE_ARTOO_INPUT_REPORT_MESSAGE: {
                        final double timestamp = packetBuffer.getDouble();
                        final short gimbalY = packetBuffer.getShort();
                        final short gimbalRate = packetBuffer.getShort();
                        final short battery = packetBuffer.getShort();

                        packet = new ControllerMessageInputReport(timestamp, gimbalY, gimbalRate, battery);
                        break;
                    }

                    case TYPE_SOLO_GOPRO_SET_REQUEST: {
                        final short command = packetBuffer.getShort();
                        final short value = packetBuffer.getShort();
                        packet = new SoloGoproSetRequest(command, value);
                        break;
                    }

                    case TYPE_SOLO_GOPRO_RECORD: {
                        @SoloGoproConstants.RecordCommand final int command = packetBuffer.getInt();
                        packet = new SoloGoproRecord(command);
                        break;
                    }

                    case TYPE_SOLO_GOPRO_STATE: {
                        packet = new SoloGoproState(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_GOPRO_STATE_V2:{
                        packet = new SoloGoproStateV2(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_GOPRO_REQUEST_STATE: {
                        packet = new SoloGoproRequestState();
                        break;
                    }

                    case TYPE_SOLO_GOPRO_SET_EXTENDED_REQUEST: {
                        final short command = packetBuffer.getShort();
                        final byte[] values = new byte[4];
                        packetBuffer.get(values);
                        packet = new SoloGoproSetExtendedRequest(command, values);
                        break;
                    }

                    case TYPE_SOLO_SPLINE_RECORD: {
                        packet = new SoloSplineRecord();
                        break;
                    }

                    case TYPE_SOLO_SPLINE_PLAY: {
                        packet = new SoloSplinePlay();
                        break;
                    }

                    case TYPE_SOLO_SPLINE_POINT:{
                        packet = new SoloSplinePoint(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_SPLINE_SEEK:{
                        packet = new SoloSplineSeek(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_SPLINE_PLAYBACK_STATUS:{
                        packet = new SoloSplinePlaybackStatus(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_SPLINE_PATH_SETTINGS:{
                        packet = new SoloSplinePathSettings(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_SPLINE_DURATIONS:{
                        packet = new SoloSplineDurations(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_SPLINE_ATTACH: {
                        packet = new SoloSplineAttach(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_PANO_OPTIONS: {
                        packet = new SoloPanoOptions(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_PANO_STATUS: {
                        packet = new SoloPanoStatus(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_ZIPLINE_OPTIONS: {
                        packet = new SoloZiplineOptions(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_ZIPLINE_LOCK: {
                        packet = new SoloZiplineLock();
                        break;
                    }

                    case TYPE_RTL_HOME_POINT: {
                        final double latitude = packetBuffer.getDouble();
                        final double longitude = packetBuffer.getDouble();
                        final float altitude = packetBuffer.getFloat();
                        packet = new SoloReturnHomeLocationMessage(latitude, longitude, altitude);
                        break;
                    }

                    case TYPE_SOLO_REWIND_OPTIONS: {
                        packet = new SoloRewindOptions(packetBuffer);
                        break;
                    }

                    /************************ Site Scan tlv packets **************/
                    case TYPE_SOLO_INSPECT_START:{
                        packet = new SoloInspectStart(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_INSPECT_SET_WAYPOINT:{
                        packet = new SoloInspectSetWaypoint(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_INSPECT_MOVE_GIMBAL:{
                        packet = new SoloInspectMoveGimbal(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_INSPECT_MOVE_VEHICLE:{
                        packet = new SoloInspectMoveVehicle(packetBuffer);
                        break;
                    }

                    case TYPE_SOLO_SCAN_START:{
                        packet = new SoloScanStart();
                        break;
                    }

                    case TYPE_SOLO_SURVEY_START:{
                        packet = new SoloSurveyStart();
                        break;
                    }

                    case TYPE_SOLO_PAUSE_BUTTON:{
                        packet = new SoloPause();
                        break;
                    }

                    default:
                        break;
                }

                if (packet != null && packet.getMessageLength() == messageLength) {
                    packetList.add(packet);
                } else {
                    packetBuffer.reset();
                    packetBuffer.position(packetBuffer.position() + messageLength);
                }
            }

        } catch (BufferUnderflowException e) {
            Log.e(TAG, "Invalid data for tlv packet of type " + messageType);
        }

        packetBuffer.order(originalOrder);
        return packetList;
    }

    //Private constructor to prevent instantiation
    private TLVMessageParser() {}
}
