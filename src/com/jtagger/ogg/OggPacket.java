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

    void write(byte[] buffer) {

        if (buffer.length <= packetData.length - index) {
            System.arraycopy(buffer, 0, packetData, index, buffer.length);
            index += buffer.length;
            return;
        }

        packetData = Arrays.copyOf(packetData, index + buffer.length);
        System.arraycopy(buffer, 0, packetData, index, buffer.length);
        index += buffer.length;
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
