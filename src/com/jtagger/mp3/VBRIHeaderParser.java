package com.jtagger.mp3;

import java.util.Arrays;

import static com.jtagger.utils.IntegerUtils.toUInt16BE;
import static com.jtagger.utils.IntegerUtils.toUInt32BE;
import static java.lang.Float.intBitsToFloat;

public class VBRIHeaderParser {

    public VBRIHeader parse(MpegFrame frame) {

        int offset = 0;

        byte[] header = Arrays.copyOfRange(frame.getFrameBody(), 32, 32 + 18);
        byte[] magic  = Arrays.copyOfRange(header, offset, 4); offset += 4;

        if (!Arrays.equals(magic, VBRIHeader.VBRI_MAGIC)) {
            System.err.println("VBRIHeader signature mismatch");
            return null;
        }

        short version = toUInt16BE(Arrays.copyOfRange(header, offset, 2));
        offset += 2;

        float delay = intBitsToFloat(toUInt16BE(Arrays.copyOfRange(header, offset, 2)));
        offset += 2;

        short quality = toUInt16BE(Arrays.copyOfRange(header, offset, 2));
        offset += 2;

        int totalBytes = toUInt32BE(Arrays.copyOfRange(header, offset, 4));
        offset += 4;

        int totalFrames = toUInt32BE(Arrays.copyOfRange(header, offset, 4));
        offset += 4;

        return new VBRIHeader(
                version,
                quality,
                delay,
                totalBytes,
                totalFrames
        );
    }
}
