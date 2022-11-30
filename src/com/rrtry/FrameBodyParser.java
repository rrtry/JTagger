package com.rrtry;

public interface FrameBodyParser<T extends AbstractFrame> {

    T parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader);
}
