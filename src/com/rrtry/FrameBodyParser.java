package com.rrtry;

public interface FrameBodyParser<T extends AbstractFrame> {

    T parse(TagHeader tagHeader, FrameHeader frameHeader, byte[] frameData);
}
