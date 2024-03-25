package com.jtagger.mp3.id3;

import java.time.format.DateTimeParseException;
import java.util.Arrays;

public class TextFrameParser implements FrameBodyParser<TextFrame> {

    @Override
    public final TextFrame parse(
            String identifier,
            FrameHeader frameHeader,
            byte[] frameData,
            TagHeader tagHeader)
    {
        byte encoding = frameData[0];
        String text   = new String(
                Arrays.copyOfRange(frameData, 1, frameData.length),
                TextEncoding.getCharset(encoding)
        ).replace("\0", "");

        if (TimestampFrame.V24_IDENTIFIERS.contains(identifier) ||
            TimestampFrame.V23_IDENTIFIERS.contains(identifier))
        {
            try {
                return TimestampFrame.createInstance(identifier, tagHeader.getMajorVersion(), text);
            }
            catch (DateTimeParseException | IllegalArgumentException e) {
                return null;
            }
        }
        return TextFrame.newBuilder()
                .setHeader(frameHeader)
                .setText(text)
                .setEncoding(encoding)
                .build(frameData);
    }
}
