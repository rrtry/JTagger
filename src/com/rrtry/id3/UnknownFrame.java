package com.rrtry.id3;

public class UnknownFrame extends AbstractFrame {

    public UnknownFrame(FrameHeader header, byte[] frameBytes) {
        this.header = header;
        this.frameBytes = frameBytes;
    }

    @Override
    public String toString() {
        return String.format("ID: %s, UNKNOWN FRAME", getIdentifier());
    }

    @Override
    public byte[] assemble(byte version) {
        return frameBytes;
    }

    @Override
    public byte[] getBytes() {
        return frameBytes;
    }
}
