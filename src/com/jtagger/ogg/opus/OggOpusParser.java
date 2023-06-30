package com.jtagger.ogg.opus;

import com.jtagger.StreamInfo;
import com.jtagger.StreamInfoParser;
import com.jtagger.ogg.*;
import com.jtagger.ogg.vorbis.VorbisComments;
import com.jtagger.ogg.vorbis.VorbisCommentsParser;

import java.io.RandomAccessFile;
import java.util.Arrays;

import static com.jtagger.utils.IntegerUtils.toUInt16LE;
import static com.jtagger.utils.IntegerUtils.toUInt32LE;

public class OggOpusParser extends OggParser implements StreamInfoParser<OpusIdentificationHeader> {

    public static final byte[] OPUS_IDENTIFICATION_HEADER_MAGIC = new byte[] {
            0x4F, 0x70, 0x75, 0x73, 0x48, 0x65, 0x61, 0x64
    };

    public static final byte[] OPUS_COMMENT_HEADER_MAGIC = new byte[] {
            0x4F, 0x70, 0x75, 0x73, 0x54, 0x61, 0x67, 0x73
    };

    private VorbisComments tag;

    public OggOpusParser(VorbisComments tag) {
        this.tag = tag;
    }

    public OggOpusParser() {

    }

    private boolean isHeaderSignatureInvalid(byte[] header, byte[] magic) {
        return !Arrays.equals(
                Arrays.copyOfRange(header, 0, magic.length),
                magic
        );
    }

    private int getBitrate(StreamInfo streamInfo) {

        int length = 0;
        for (int i = 2; i < packets.size(); i++) {
            length += packets.get(i).getSize();
        }

        return (int) Math.ceil((length * 8f) / (getDuration(streamInfo) * 1000f));
    }

    @Override
    public OpusIdentificationHeader parseStreamInfo(RandomAccessFile file) {

        parsePackets(parsePages(file));

        OggPacket packet = packets.get(0);
        byte[] header    = packet.getPacketData();

        if (isHeaderSignatureInvalid(header, OPUS_IDENTIFICATION_HEADER_MAGIC)) {
            throw new IllegalStateException("Header signature mismatch");
        }

        int offset = OPUS_IDENTIFICATION_HEADER_MAGIC.length;

        byte version      = header[offset++];
        byte channelCount = header[offset++];

        short preSkip    = toUInt16LE(Arrays.copyOfRange(header, offset, offset + 2)); offset += 2;
        int sampleRate   = toUInt32LE(Arrays.copyOfRange(header, offset, offset + 4)); offset += 4;
        short outputGain = toUInt16LE(Arrays.copyOfRange(header, offset, offset + 2)); offset += 2;

        byte channelMappingFamily = header[offset++];

        OpusIdentificationHeader opusHeader;
        if (channelMappingFamily == 0x0) {

            opusHeader = new OpusIdentificationHeader(
                    channelCount,
                    channelMappingFamily,
                    preSkip,
                    outputGain,
                    sampleRate,
                    header
            );

            opusHeader.setBitrate(getBitrate(opusHeader));
            opusHeader.setDuration(getDuration(opusHeader));

            return opusHeader;
        }

        byte streamCount      = header[offset++];
        byte coupledCount     = header[offset++];
        byte[] channelMapping = Arrays.copyOfRange(header, offset, offset + Byte.toUnsignedInt(channelCount));

        opusHeader = new OpusIdentificationHeader(
                channelCount, channelMappingFamily, streamCount, coupledCount,
                preSkip, outputGain, sampleRate, channelMapping, header
        );

        opusHeader.setDuration(getDuration(opusHeader));
        opusHeader.setBitrate(getBitrate(opusHeader));

        return opusHeader;
    }

    @Override
    public VorbisComments parseTag(RandomAccessFile file) {

        if (tag != null) return tag;
        parsePackets(parsePages(file));

        OggPacket packet = packets.get(1);
        byte[] header    = packet.getPacketData();

        if (isHeaderSignatureInvalid(header, OPUS_COMMENT_HEADER_MAGIC)) {
            throw new IllegalArgumentException("Header signature mismatch");
        }

        VorbisCommentsParser parser = new VorbisCommentsParser();
        return parser.parse(Arrays.copyOfRange(header, OPUS_IDENTIFICATION_HEADER_MAGIC.length, header.length), false);
    }
}
