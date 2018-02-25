package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import com.MAVLink.enums.GOPRO_COMMAND;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the TLVMessageParser class.
 */
public class TLVMessageParserTest {

    @Test
    public void testParseTLVPacket_nullParams() throws Exception {
        List<TLVPacket> results;

        //Test for invalid parameters
        results = TLVMessageParser.parseTLVPacket((byte[]) null);
        assertNull(results);
    }

    @Test
    public void testParseTLVPacket_emptyParams() {
        List<TLVPacket> results;

        results = TLVMessageParser.parseTLVPacket(new byte[0]);
        assertNull(results);
    }

    @Test
    public void testParseTLVPacket_singleMessage() {
        List<TLVPacket> results;

        //Single message parsing test
        SoloMessageLocation messageLoc = new SoloMessageLocation(5.3488066, -4.0499032, 10);
        byte[] messageLocBytes = messageLoc.toBytes();
        results = TLVMessageParser.parseTLVPacket(messageLocBytes);
        assertNotNull(results);
        assertTrue(results.size() == 1);

        TLVPacket resultPacket = results.get(0);
        assertTrue(resultPacket.getMessageLength() == messageLoc.getMessageLength());
        assertTrue(resultPacket.getMessageType() == messageLoc.getMessageType());
        assertTrue(resultPacket instanceof SoloMessageLocation);

        SoloMessageLocation castedResult = (SoloMessageLocation) resultPacket;
        assertTrue(castedResult.getCoordinate().equals(messageLoc.getCoordinate()));

    }

    @Test
    public void testParseTLVPacket_multipleMessages() {
        List<TLVPacket> results;
        SoloMessageLocation messageLoc = new SoloMessageLocation(5.3488066, -4.0499032, 10);

        //Multiple message parsing test
        //1.
        ByteBuffer inputData = ByteBuffer.allocate(46);
        SoloMessageShotGetter shotGetter = new SoloMessageShotGetter(SoloMessageShot.SHOT_CABLECAM);
        TLVPacket[] inputPackets = {messageLoc, shotGetter};
        for (TLVPacket packet : inputPackets) {
            inputData.put(packet.toBytes());
        }
        inputData.rewind();

        results = TLVMessageParser.parseTLVPacket(inputData);
        assertNotNull(results);
        assertTrue(results.size() == inputPackets.length);
        assertTrue(inputData.remaining() == 6);

        for (int i = 0; i < inputPackets.length; i++) {
            TLVPacket inputPacket = inputPackets[i];
            TLVPacket outputPacket = results.get(i);
            assertTrue(inputPacket.equals(outputPacket));
        }

    }

    @Test
    public void testParseTLVPacket_multipleMessages2() {
        List<TLVPacket> results;

        //2.
        SoloMessageShotGetter shotGetter = new SoloMessageShotGetter(SoloMessageShot.SHOT_CABLECAM);
        SoloMessageLocation messageLoc = new SoloMessageLocation(5.3488066, -4.0499032, 10);
        ByteBuffer inputData = ByteBuffer.allocate(40);
        TLVPacket[] inputPackets = {messageLoc, shotGetter};
        for (TLVPacket packet : inputPackets) {
            inputData.put(packet.toBytes());
        }
        inputData.rewind();

        results = TLVMessageParser.parseTLVPacket(inputData);
        assertNotNull(results);
        assertTrue(results.size() == inputPackets.length);
        assertTrue(inputData.remaining() == 0);

        for (int i = 0; i < inputPackets.length; i++) {
            TLVPacket inputPacket = inputPackets[i];
            TLVPacket outputPacket = results.get(i);
            assertTrue(inputPacket.equals(outputPacket));
        }
    }

    @Test
    public void testParseTLVPacket_goProSetExtended() {
        byte[] values = new byte[4];

        SoloGoproSetExtendedRequest extendedRequest =
            new SoloGoproSetExtendedRequest((short) GOPRO_COMMAND.GOPRO_COMMAND_CAPTURE_MODE, values);

        TLVPacket[] inputPackets = {extendedRequest};

        ByteBuffer inputData = ByteBuffer.allocate(14);
        for (TLVPacket packet : inputPackets) {
            inputData.put(packet.toBytes());
        }
        inputData.rewind();

        List<TLVPacket> results = TLVMessageParser.parseTLVPacket(inputData);
        assertNotNull(results);
        assertEquals(inputPackets.length, results.size());
        assertEquals(0, inputData.remaining());

        assertEquals(extendedRequest, results.get(0));
    }

    @Test
    public void testParseTLVPacket_goProSetRequest() {
        SoloGoproSetRequest setRequest =
            new SoloGoproSetRequest((short) GOPRO_COMMAND.GOPRO_COMMAND_CAPTURE_MODE, SoloGoproConstants.CAPTURE_MODE_PHOTO);

        TLVPacket[] inputPackets = {setRequest};

        ByteBuffer inputData = ByteBuffer.allocate(12);
        for (TLVPacket packet : inputPackets) {
            inputData.put(packet.toBytes());
        }
        inputData.rewind();

        List<TLVPacket> results = TLVMessageParser.parseTLVPacket(inputData);
        assertNotNull(results);
        assertEquals(inputPackets.length, results.size());
        assertEquals(0, inputData.remaining());

        assertEquals(setRequest, results.get(0));
    }

    @Test
    public void testParseTLVPacket_goProRecord() {
        SoloGoproRecord record =
            new SoloGoproRecord(SoloGoproConstants.STOP_RECORDING);

        TLVPacket[] inputPackets = {record};

        ByteBuffer inputData = ByteBuffer.allocate(12);
        for (TLVPacket packet : inputPackets) {
            inputData.put(packet.toBytes());
        }
        inputData.rewind();

        List<TLVPacket> results = TLVMessageParser.parseTLVPacket(inputData);
        assertNotNull(results);
        assertEquals(0, inputData.remaining());

        assertEquals(record, results.get(0));
    }

    @Test
    public void testParseTLVPacket_goProState() {
        SoloGoproState state =
            new SoloGoproState(
                (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1,
                (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1,
                (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1,
                (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1);

        TLVPacket[] inputPackets = {state};

        ByteBuffer inputData = ByteBuffer.allocate(44);
        for (TLVPacket packet : inputPackets) {
            inputData.put(packet.toBytes());
        }
        inputData.rewind();

        List<TLVPacket> results = TLVMessageParser.parseTLVPacket(inputData);
        assertNotNull(results);
        assertEquals(inputPackets.length, results.size());
        assertEquals(0, inputData.remaining());

        assertEquals(state, results.get(0));
    }

    @Test
    public void testParseTLVPacket_goProRequestState() {
        SoloGoproRequestState requestState =
            new SoloGoproRequestState();

        TLVPacket[] inputPackets = {requestState};

        ByteBuffer inputData = ByteBuffer.allocate(8);
        for (TLVPacket packet : inputPackets) {
            inputData.put(packet.toBytes());
        }
        inputData.rewind();

        List<TLVPacket> results = TLVMessageParser.parseTLVPacket(inputData);
        assertNotNull(results);
        assertEquals(inputPackets.length, results.size());
        assertEquals(0, inputData.remaining());

        assertEquals(requestState, results.get(0));
    }
}