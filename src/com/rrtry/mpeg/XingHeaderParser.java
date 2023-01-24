package com.rrtry.mpeg;

import com.rrtry.utils.IntegerUtils;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.Arrays;

import static com.rrtry.mpeg.MpegFrameHeader.CHANNEL_MODE_SINGLE_CHANNEL;
import static com.rrtry.mpeg.MpegFrameHeader.MPEG_VERSION_1;
import static com.rrtry.mpeg.XingHeader.*;
import static com.rrtry.utils.IntegerUtils.toUInt32BE;

public class XingHeaderParser {

    public XingHeader parse(MpegFrame frame) {

        MpegFrameHeader header = frame.getMpegHeader();
        byte[] frameBody       = frame.getFrameBody();

        final byte channelMode = header.getChannelMode();
        int offset;

        if (channelMode != CHANNEL_MODE_SINGLE_CHANNEL) {
            offset = header.getVersion() == MPEG_VERSION_1 ? 32 : 17;
        } else {
            offset = header.getVersion() == MPEG_VERSION_1 ? 17 : 9;
        }

        byte[] magic = Arrays.copyOfRange(frameBody, offset, offset + 4);
        if (!Arrays.equals(magic, XingHeader.XING_VBR_MAGIC) &&
                !Arrays.equals(magic, XingHeader.XING_CBR_MAGIC))
        {
            return null;
        }

        offset += 4;
        int flags = toUInt32BE(Arrays.copyOfRange(frameBody, offset, offset + 4)); offset += 4;

        int totalFrames;
        int totalBytes;
        int quality;
        int entries;

        byte[] tableOfContents = new byte[100];

        XingHeader.Builder builder = XingHeader.newBuilder();
        builder = builder.setMagic(magic);
        builder = builder.setFlags(flags);

        if ((flags & FLAG_FRAMES) != 0x0) {
            totalFrames = toUInt32BE(Arrays.copyOfRange(frameBody, offset, offset + 4)); offset += 4;
            builder     = builder.setTotalFrames(totalFrames);
        }
        if ((flags & FLAG_BYTES) != 0x0) {
            totalBytes  = toUInt32BE(Arrays.copyOfRange(frameBody, offset, offset + 4)); offset += 4;
            builder     = builder.setTotalBytes(totalBytes);
        }
        if ((flags & FLAG_TOC) != 0x0) {
            entries = toUInt32BE(Arrays.copyOfRange(frameBody, offset, offset + 4)); offset += 4;
            builder = builder.setTableOfContents(tableOfContents);
        }
        if ((flags & FLAG_QUALITY) != 0x0) {
            quality = toUInt32BE(Arrays.copyOfRange(frameBody, offset, offset + 4)); offset += 4;
            builder = builder.setQualityIndicator(quality);
        }

        return builder.build();
    }
}
