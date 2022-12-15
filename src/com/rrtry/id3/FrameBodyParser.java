package com.rrtry.id3;

public interface FrameBodyParser<T extends AbstractFrame> {

    T parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader);
}
