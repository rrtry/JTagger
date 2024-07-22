package com.jtagger.ogg;

import java.util.Arrays;

public class OggPacket {

    private int index = 0;
    private byte[] packetData = new byte[255];

    public OggPacket() {
        /* empty constructor */
    }

    public OggPacket(byte[] packetData) {
        this.index      = packetData.length;
        this.packetData = packetData;
    }

    void write(byte[] buffer, int offset, int length) {

        if (length == 0) {
            return;
        }
        if (length <= packetData.length - index) {
            System.arraycopy(buffer, offset, packetData, index, length);
            index += length;
            return;
        }

        packetData = Arrays.copyOf(packetData, index + length);
        System.arraycopy(buffer, offset, packetData, index, length);
        index += length;
    }

    public byte[] getBuffer() {
        return packetData;
    }

    public byte[] getData() {
        return index < packetData.length ?
                Arrays.copyOf(packetData, index) :
                packetData;
    }

    public int getSize() {
        return getData().length;
    }
}
