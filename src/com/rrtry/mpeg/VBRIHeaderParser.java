package com.rrtry.mpeg;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.Arrays;

import static com.rrtry.utils.IntegerUtils.toUInt16BE;
import static com.rrtry.utils.IntegerUtils.toUInt32BE;
import static java.lang.Float.intBitsToFloat;

public class VBRIHeaderParser {

    public VBRIHeader parse(RandomAccessFile file, MpegFrameHeader mpegHeader) {
        try {

            int offset = 0;
            file.seek(mpegHeader.getOffset() + 4 + 32);

            byte[] header = new byte[18];
            file.read(header, 0, header.length);

            byte[] magic = Arrays.copyOfRange(header, offset, 4); offset += 4;
            if (!Arrays.equals(magic, VBRIHeader.VBRI_MAGIC)) {
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

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
