package com.rrtry.mpeg;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.Arrays;

import static com.rrtry.mpeg.MpegFrameHeader.CHANNEL_MODE_SINGLE_CHANNEL;
import static com.rrtry.mpeg.MpegFrameHeader.MPEG_VERSION_1;
import static com.rrtry.mpeg.XingHeader.*;

public class XingHeaderParser {

    public XingHeader parse(RandomAccessFile file, MpegFrameHeader header) {
        try {

            final byte channelMode = header.getChannelMode();
            byte offset;

            if (channelMode != CHANNEL_MODE_SINGLE_CHANNEL) {
                offset = (byte) (header.getVersion() == MPEG_VERSION_1 ? 32 : 17);
            } else {
                offset = (byte) (header.getVersion() == MPEG_VERSION_1 ? 17 : 9);
            }

            file.seek(header.getOffset() + offset + 4);
            byte[] magic = new byte[4];
            file.read(magic, 0, magic.length);

            if (!Arrays.equals(magic, XingHeader.XING_VBR_MAGIC) &&
                !Arrays.equals(magic, XingHeader.XING_CBR_MAGIC))
            {
                return null;
            }

            int flags = file.readInt();

            int totalFrames;
            int totalBytes;
            int quality;
            int entries;

            byte[] tableOfContents = new byte[100];

            XingHeader.Builder builder = XingHeader.newBuilder();
            builder = builder.setMagic(magic);
            builder = builder.setFlags(flags);

            if ((flags & FLAG_FRAMES) != 0x0) {
                totalFrames = file.readInt();
                builder     = builder.setTotalFrames(totalFrames);
            }
            if ((flags & FLAG_BYTES) != 0x0) {
                totalBytes  = file.readInt();
                builder     = builder.setTotalBytes(totalBytes);
            }
            if ((flags & FLAG_TOC) != 0x0) {
                entries = file.read(tableOfContents);
                builder = builder.setTableOfContents(tableOfContents);
            }
            if ((flags & FLAG_QUALITY) != 0x0) {
                quality = file.readInt();
                builder = builder.setQualityIndicator(quality);
            }

            return builder.build();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
