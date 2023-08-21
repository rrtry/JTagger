package com.jtagger.ogg.flac;

import com.jtagger.StreamInfoParser;
import com.jtagger.flac.*;
import com.jtagger.ogg.OggParser;
import com.jtagger.ogg.vorbis.VorbisComments;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import static com.jtagger.flac.AbstractMetadataBlock.BLOCK_TYPE_STREAMINFO;
import static com.jtagger.flac.AbstractMetadataBlock.BLOCK_TYPE_VORBIS_COMMENT;
import static com.jtagger.flac.FlacParser.getBitrate;
import static com.jtagger.utils.IntegerUtils.toUInt16BE;
import static com.jtagger.utils.IntegerUtils.toUInt24BE;

public class OggFlacParser extends OggParser implements StreamInfoParser<StreamInfoBlock> {

    public static final int HEADER_PACKETS_OFFSET    = 7;
    public static final int FLAC_BLOCK_HEADER_OFFSET = 13;

    private VorbisComments vorbis;
    private StreamInfoBlock streamInfoBlock;
    private int headerPackets;

    @Override
    public VorbisComments parseTag(RandomAccessFile file) throws IOException {

        if (vorbis != null) return vorbis;
        parseStreamInfo(file);

        for (int i = 0; i < headerPackets; i++) {

            byte[] packet = packets.get(i + 1).getPacketDataTruncated();
            int index     = 0;

            byte blockHeader = packet[index++];
            int blockType    = blockHeader & 0x7F;
            int blockLength  = toUInt24BE(Arrays.copyOfRange(packet, index, index + 3)); index += 3;

            if (!FlacParser.isValidBlockType(blockType)) {
                throw new IllegalStateException("Invalid packet type");
            }

            if (blockType == BLOCK_TYPE_VORBIS_COMMENT) {
                VorbisCommentBlockParser parser = new VorbisCommentBlockParser();
                vorbis = parser.parse(Arrays.copyOfRange(packet, index, index + blockLength))
                        .getVorbisComments();
                break; /* Break here 'cause ffprobe does not recognize artwork embedded in FLAC's native METADATA_BLOCK_PICTURE, so the only block we need is VORBIS_COMMENT */
                      /* Artwork embedding is implemented according to OGG format specification */
            }
        }
        return vorbis;
    }

    @Override
    public StreamInfoBlock parseStreamInfo(RandomAccessFile file) {

        if (streamInfoBlock != null) return streamInfoBlock;
        parsePackets(parsePages(file));

        byte[] mappingPacket = packets.get(0).getPacketDataTruncated();
        int index = 0;

        if (mappingPacket[index++] != 0x7F) {
            throw new IllegalStateException("Invalid packet type");
        }

        String signature = new String(Arrays.copyOfRange(mappingPacket, index, index + 4)); index += 4;
        if (!signature.equals("FLAC")) {
            throw new IllegalStateException("Invalid packet signature");
        }

        int majorVersion = mappingPacket[index++];
        int minorVersion = mappingPacket[index++];

        if (majorVersion != 0x01) throw new IllegalStateException("Unsupported version");
        if (minorVersion != 0x00) throw new IllegalStateException("Unsupported version");

        headerPackets = toUInt16BE(Arrays.copyOfRange(mappingPacket, index, index + 2)); index += 2;
        signature     = new String(Arrays.copyOfRange(mappingPacket, index, index + 4)); index += 4;

        if (!signature.equals("fLaC")) {
            throw new IllegalStateException("Invalid native flac signature");
        }

        int blockType   = mappingPacket[index++] & 0x7F;
        int blockLength = toUInt24BE(Arrays.copyOfRange(mappingPacket, index, index + 3)); index += 3;

        if (blockType != BLOCK_TYPE_STREAMINFO) {
            throw new IllegalStateException("Expected block type STREAMINFO");
        }

        StreamInfoBlockParser parser = new StreamInfoBlockParser();
        streamInfoBlock = parser.parse(Arrays.copyOfRange(mappingPacket, index, index + blockLength));

        int streamLength = 0;
        for (int i = packets.size() - 1; i > headerPackets - 1; i--) {
            streamLength += packets.get(i).getPacketDataTruncated().length;
        }

        streamInfoBlock.setTotalSamples((int) pages.get(pages.size() - 1).getHeader().getGranulePosition());
        streamInfoBlock.setBitrate(getBitrate(streamInfoBlock, streamLength));
        return streamInfoBlock;
    }
}
