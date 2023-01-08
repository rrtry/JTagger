package com.rrtry.mpeg.id3;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UnknownFrame extends AbstractFrame<List<Byte>> {

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
    List<Byte> getFrameData() {
        return IntStream.range(0, frameBytes.length).mapToObj(i -> frameBytes[i]).collect(Collectors.toList());
    }

    @Override
    void setFrameData(List<Byte> data) {
        frameBytes = new byte[data.size()];
        IntStream.range(0, data.size()).forEach(i -> frameBytes[i] = data.get(i));
    }

    @Override
    public byte[] getBytes() {
        return frameBytes;
    }
}
