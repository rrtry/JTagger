package com.jtagger.mp3.id3;

import java.nio.charset.Charset;
import java.util.Arrays;

public class UserDefinedTextInfoFrameParser extends ContentDescriptionFrameParser<UserDefinedTextInfoFrame> {

    @Override
    public UserDefinedTextInfoFrame parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader) {

        byte encoding;
        String description;
        String text;
        Charset charset;

        encoding    = frameData[position++];
        charset     = TextEncoding.getCharset(encoding);
        description = parseContentDescription(frameData, charset);
        text        = new String(Arrays.copyOfRange(frameData, position, frameData.length), charset).trim();

        return UserDefinedTextInfoFrame.newBuilder()
                .setHeader(frameHeader)
                .setEncoding(encoding)
                .setDescription(description)
                .setText(text)
                .build(tagHeader.getMajorVersion());
    }
}
