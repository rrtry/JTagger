package com.rrtry.ogg.vorbis;

import com.rrtry.ogg.OggPacket;
import com.rrtry.ogg.OggParser;
import com.rrtry.utils.IntegerUtils;

import java.io.RandomAccessFile;
import java.util.Arrays;

public class OggVorbisParser extends OggParser {

    public static final byte INVALID_HEADER_TYPE        = 0x1;
    public static final byte INVALID_HEADER_MAGIC       = 0x2;
    public static final byte INVALID_HEADER_FRAMING_BIT = 0x3;

    private byte verifyHeader(byte[] packetData, byte type) {

        if (packetData[0] != type) return INVALID_HEADER_TYPE;
        if (!Arrays.equals(Arrays.copyOfRange(packetData, 1, 7), VorbisHeader.VORBIS_HEADER_MAGIC)) return INVALID_HEADER_MAGIC;
        if (packetData[packetData.length - 1] != 0x1) return INVALID_HEADER_FRAMING_BIT;

        return 0;
    }

    @Override
    public VorbisComments parseVorbisComments(RandomAccessFile file) {
        return parseVorbisCommentHeader(file).getVorbisComments();
    }

    public VorbisCommentHeader parseVorbisCommentHeader(RandomAccessFile file) {

        parsePackets(parsePages(file));

        if (packets.size() < 2) return null;
        OggPacket oggPacket = packets.get(1);

        byte[] packet = oggPacket.getPacketData();
        byte headerValid = verifyHeader(packet, VorbisHeader.HEADER_TYPE_COMMENT);

        if (headerValid != 0x0) {
            throw new IllegalStateException("Invalid vorbis comment header, error: " + headerValid);
        }

        byte[] headerData = Arrays.copyOfRange(packet, 7, packet.length);

        VorbisCommentsParser parser       = new VorbisCommentsParser();
        VorbisCommentHeader commentHeader = new VorbisCommentHeader();
        commentHeader.setVorbisComments(parser.parse(headerData, true));

        return commentHeader;
    }

    public VorbisIdentificationHeader parseVorbisIdentificationHeader(RandomAccessFile file) {

        parsePackets(parsePages(file));
        OggPacket oggPacket = packets.get(0);

        byte offset = 0;

        byte[] packet    = oggPacket.getPacketData();
        byte headerValid = verifyHeader(packet, VorbisHeader.HEADER_TYPE_IDENTIFICATION);

        if (headerValid != 0x0) {
            throw new IllegalStateException("Invalid vorbis identification header, error: " + headerValid);
        }

        byte[] headerData = Arrays.copyOfRange(packet, 7, packet.length);
        byte[] bytes;

        int version;
        int sampleRate;
        int maxBitrate;
        int nominalBitrate;
        int minBitrate;

        byte channels;

        bytes = Arrays.copyOfRange(headerData, offset, offset + 4); offset += 4;
        version = IntegerUtils.toUInt32LE(bytes);
        channels = headerData[offset++];

        bytes = Arrays.copyOfRange(headerData, offset, offset + 4); sampleRate     = IntegerUtils.toUInt32LE(bytes); offset += 4;
        bytes = Arrays.copyOfRange(headerData, offset, offset + 4); maxBitrate     = IntegerUtils.toUInt32LE(bytes); offset += 4;
        bytes = Arrays.copyOfRange(headerData, offset, offset + 4); nominalBitrate = IntegerUtils.toUInt32LE(bytes); offset += 4;
        bytes = Arrays.copyOfRange(headerData, offset, offset + 4); minBitrate     = IntegerUtils.toUInt32LE(bytes); offset += 4;

        byte blockSize  = headerData[offset++];
        return new VorbisIdentificationHeader(
                version, channels, sampleRate, maxBitrate, nominalBitrate, minBitrate, blockSize, packet
        );
    }
}
