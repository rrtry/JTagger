package com.jtagger.mp3.id3;

import java.nio.charset.Charset;
import java.util.Arrays;

import static com.jtagger.mp3.id3.AttachedPictureFrame.DESCRIPTION_MAX_LENGTH;

abstract class ContentDescriptionFrameParser<T extends AbstractFrame> implements FrameBodyParser<T> {

    protected int position = 0;

    protected String parseContentDescription(byte[] frameData, Charset charset) {

        boolean isUTF16  = TextEncoding.isUTF16(charset);
        final int length = (isUTF16 ? 2 : 1);
        final int from   = position;
        final int to     = from + DESCRIPTION_MAX_LENGTH * length + length;

        while (position < to) {
            if (isUTF16 && frameData[position] == '\0' &&
                frameData[position + 1] == '\0')
            {
                break;
            }
            if (!isUTF16 && frameData[position] == '\0') {
                break;
            }
            position += length;
        }
        String description = new String(
                Arrays.copyOfRange(frameData, from, position), charset
        );
        position += length;
        return description;
    }
}
