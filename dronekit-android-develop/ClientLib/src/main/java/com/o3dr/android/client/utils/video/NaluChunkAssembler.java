package com.o3dr.android.client.utils.video;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * Created by Fredia Huya-Kouadio on 6/1/15.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class NaluChunkAssembler {

    private static final String TAG = NaluChunkAssembler.class.getSimpleName();

    private final NaluChunk assembledNaluChunk;
    private final NaluChunk paramsNaluChunk;
    private final NaluChunk eosNaluChunk;

    /**
     * Stores the sps data so it can be concatenate with the pps data.
     */
    private final static int SPS_BUFFER_INDEX = 0;
    private boolean isSpsSet = false;

    /**
     * Stores the pps data so it can be concatenate with the sps data.
     */
    private final static int PPS_BUFFER_INDEX = 1;
    private boolean isPpsSet = false;

    NaluChunkAssembler(){
        this.assembledNaluChunk = new NaluChunk(1, 1024 * 1024, NaluChunk.START_CODE);

        this.paramsNaluChunk = new NaluChunk(2, 256, NaluChunk.START_CODE);
        this.paramsNaluChunk.type = 78;
        this.paramsNaluChunk.flags = MediaCodec.BUFFER_FLAG_CODEC_CONFIG;

        this.eosNaluChunk = new NaluChunk(1, 0, null);
        this.eosNaluChunk.flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
    }

    void reset(){
        isSpsSet = false;
        isPpsSet = false;

        this.assembledNaluChunk.payloads[0].reset();

        this.paramsNaluChunk.payloads[0].reset();
        this.paramsNaluChunk.payloads[1].reset();
    }

    private boolean areParametersSet() {
        return isSpsSet && isPpsSet;
    }

    NaluChunk getEndOfStream(){
        return eosNaluChunk;
    }

    private int prevSeq = -1;
    private int naluCounter = 0;
    private final static long DELTA_PRESENTATION_TIME = 42000L;

    NaluChunk getParametersSet(){
        if(areParametersSet())
            return paramsNaluChunk;

        return null;
    }

    NaluChunk assembleNALUChunk(byte[] buffer, int bufferLength) {

        //The first 12 bytes are the rtp header.
        final byte nalHeaderByte = buffer[12];
        final int forbiddenBit = (nalHeaderByte & 0x80) >> 7;
        if (forbiddenBit != 0) {
            Log.w(TAG, "Forbidden bit is set, indicating possible errors.");
            return null;
        }

        long rtpTimestamp = 0;
        rtpTimestamp |= (buffer[4] & 0xffl) << 24;
        rtpTimestamp |= (buffer[5] & 0xffl) << 16;
        rtpTimestamp |= (buffer[6] & 0xffl) << 8;
        rtpTimestamp |= (buffer[7] & 0xffl);

        final int sequenceNumber = ((buffer[2] & 0xff) << 8) | (buffer[3] & 0xff);
        final int nalType = nalHeaderByte & 0x1f;
        if (nalType <= 0) {
            Log.d(TAG, "Undefined nal type: " + nalType);
            return null;
        }

        //DEBUG LOGIC
        if(prevSeq != -1){
            final int expectedSeq = prevSeq + 1;
            if(sequenceNumber != expectedSeq){
                Log.v(TAG, String.format(Locale.US, "Sequence number is out of order: %d != %d", expectedSeq, sequenceNumber));
            }
        }
        prevSeq = sequenceNumber;
        //DEBUG LOGIC

        if (nalType <= 23) {
            //Single nal unit packet.
            final int payloadOffset = 12;
            final int payloadLength = bufferLength - payloadOffset;
            switch (nalType) {
                case 7: //SPS parameters set.
                case 8: //PPS parameters set.
                {
                    ByteBuffer naluData;
                    if (nalType == NaluChunk.SPS_NAL_TYPE) {
                        naluData = paramsNaluChunk.payloads[SPS_BUFFER_INDEX];
                        isSpsSet = true;
                    } else {
                        naluData = paramsNaluChunk.payloads[PPS_BUFFER_INDEX];
                        isPpsSet = true;
                    }

                    naluData.reset();
                    naluData.put(buffer, payloadOffset, payloadLength);

                    if (areParametersSet()) {
                        paramsNaluChunk.sequenceNumber = sequenceNumber;
                        paramsNaluChunk.presentationTime = 0;
                        return paramsNaluChunk;
                    }

                    return null;
                }

                default:
                    if (!areParametersSet())
                        return null;

                    ByteBuffer assembledNaluBuffer = assembledNaluChunk.payloads[0];
                    assembledNaluBuffer.reset();
                    assembledNaluBuffer.put(buffer, payloadOffset, payloadLength);

                    assembledNaluChunk.type = nalType;
                    assembledNaluChunk.sequenceNumber = sequenceNumber;
                    assembledNaluChunk.flags = 0;
                    assembledNaluChunk.presentationTime = naluCounter++ * DELTA_PRESENTATION_TIME;
                    return assembledNaluChunk;
            }
        }

        if (nalType == 28) {
            //Fragmentation unit
            if (!areParametersSet())
                return null;

            final int payloadOffset = 14;
            final int payloadLength = bufferLength - payloadOffset;

            final int fuIndicatorByte = nalHeaderByte;
            final int fuHeaderByte = buffer[13];
            final int fuNalType = fuHeaderByte & 0x1f;
            final int startBit = (fuHeaderByte & 0x80) >> 7;
            final int endBit = (fuHeaderByte & 0x40) >> 6;

            if (startBit == 1) {
                ByteBuffer assembledNaluBuffer = assembledNaluChunk.payloads[0];
                assembledNaluBuffer.reset();

                assembledNaluBuffer.put((byte) ((fuIndicatorByte & 0xe0) | fuNalType));
                assembledNaluBuffer.put(buffer, payloadOffset, payloadLength);

                boolean isConfig = fuNalType == 7 || fuNalType == 8;

                assembledNaluChunk.sequenceNumber = sequenceNumber;
                assembledNaluChunk.type = fuNalType;
                assembledNaluChunk.flags = isConfig ? MediaCodec.BUFFER_FLAG_CODEC_CONFIG : 0;
//                assembledNaluChunk.presentationTime = rtpTimestamp;
                return null;
            } else {
                if (sequenceNumber - 1 != assembledNaluChunk.sequenceNumber) {
                    return null;
                }

                ByteBuffer assembledNaluBuffer = assembledNaluChunk.payloads[0];
                assembledNaluBuffer.put(buffer, payloadOffset, payloadLength);
                assembledNaluChunk.sequenceNumber = sequenceNumber;

                if (endBit == 1) {
                    assembledNaluChunk.presentationTime = naluCounter++ * DELTA_PRESENTATION_TIME;
                    return assembledNaluChunk;
                } else {
                    return null;
                }
            }
        }

        return null;
    }

}
