package com.rrtry.mpeg.id3;

public interface FrameBodyParser<T extends AbstractFrame> {

    T parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader);
}
