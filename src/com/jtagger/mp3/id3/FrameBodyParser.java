package com.jtagger.mp3.id3;

public interface FrameBodyParser<T extends AbstractFrame> {

    T parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader);
}
