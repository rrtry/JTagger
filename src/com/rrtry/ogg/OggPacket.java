package com.rrtry.ogg;

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

    void appendPacketData(byte[] appendedData) {

        if (appendedData.length <= packetData.length - index) {
            System.arraycopy(appendedData, 0, packetData, index, appendedData.length);
            index += appendedData.length;
            return;
        }

        packetData = Arrays.copyOf(packetData, index + appendedData.length);
        System.arraycopy(appendedData, 0, packetData, index, appendedData.length);
        index += appendedData.length;
    }

    public void setPacketData(byte[] packetData) {
        this.index = packetData.length;
        this.packetData = packetData;
    }

    public byte[] getPacketData() {
        return Arrays.copyOf(packetData, index);
    }

    public int getSize() {
        return getPacketData().length;
    }

    public boolean isEmpty() {
        return getPacketData().length == 0;
    }
}
