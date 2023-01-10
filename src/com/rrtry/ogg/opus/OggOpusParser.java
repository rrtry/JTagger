package com.rrtry.ogg.opus;

import com.rrtry.ogg.*;
import com.rrtry.ogg.vorbis.VorbisComments;
import com.rrtry.ogg.vorbis.VorbisCommentsParser;

import java.io.RandomAccessFile;
import java.util.Arrays;

import static com.rrtry.utils.IntegerUtils.toUInt16LE;
import static com.rrtry.utils.IntegerUtils.toUInt32LE;

public class OggOpusParser extends OggParser {

    public static final byte[] OPUS_HEADER_MAGIC = new byte[] {
            0x4F, 0x70, 0x75, 0x73, 0x48, 0x65, 0x61, 0x64
    };

    private boolean verifyOpusHeader(byte[] header) {
        return Arrays.equals(
                Arrays.copyOfRange(header, 0, OPUS_HEADER_MAGIC.length),
                OPUS_HEADER_MAGIC
        );
    }

    public OpusIdentificationHeader parseOpusIdentificationHeader(RandomAccessFile file) {

        parsePackets(parsePages(file));

        OggPacket packet = packets.get(0);
        byte[] header    = packet.getPacketData();

        if (!verifyOpusHeader(header)) {
            throw new IllegalStateException("Header signature mismatch");
        }

        int offset = OPUS_HEADER_MAGIC.length;

        byte version      = header[offset++];
        byte channelCount = header[offset++];

        short preSkip    = toUInt16LE(Arrays.copyOfRange(header, offset, offset + 2)); offset += 2;
        int sampleRate   = toUInt32LE(Arrays.copyOfRange(header, offset, offset + 4)); offset += 4;
        short outputGain = toUInt16LE(Arrays.copyOfRange(header, offset, offset + 2)); offset += 2;

        byte channelMappingFamily = header[offset++];

        if (channelMappingFamily == 0x0) {
            return new OpusIdentificationHeader(
                    channelCount,
                    channelMappingFamily,
                    preSkip,
                    outputGain,
                    sampleRate,
                    header
            );
        }

        byte streamCount      = header[offset++];
        byte coupledCount     = header[offset++];
        byte[] channelMapping = Arrays.copyOfRange(header, offset, offset + Byte.toUnsignedInt(channelCount));

        return new OpusIdentificationHeader(
                channelCount, channelMappingFamily, streamCount, coupledCount,
                preSkip, outputGain, sampleRate, channelMapping, header
        );
    }

    @Override
    public VorbisComments parseVorbisComments(RandomAccessFile file) {

        parsePackets(parsePages(file));

        OggPacket packet = packets.get(1);
        byte[] header    = packet.getPacketData();

        if (!verifyOpusHeader(header)) {
            throw new IllegalArgumentException("Header signature mismatch");
        }

        VorbisCommentsParser parser = new VorbisCommentsParser();
        return parser.parse(Arrays.copyOfRange(header, OPUS_HEADER_MAGIC.length, header.length), false);
    }
}
