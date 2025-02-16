package com.jtagger.flac;

import com.jtagger.StreamInfo;
import com.jtagger.StreamInfoParser;
import com.jtagger.TagParser;
import com.jtagger.utils.IntegerUtils;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.jtagger.flac.FLAC.*;
import static com.jtagger.flac.AbstractMetadataBlock.*;

public class FlacParser implements TagParser<FLAC>, StreamInfoParser<StreamInfoBlock> {

    private FLAC flac;
    private int streamOffset = 0;

    private final StreamInfoBlockParser streamInfoParser = new StreamInfoBlockParser();
    private final VorbisCommentBlockParser commentParser = new VorbisCommentBlockParser();
    private final PictureBlockParser pictureParser       = new PictureBlockParser();

    public static boolean isValidBlockType(int blockType) {
        return blockType >= 0x00 && blockType <= 0x7E;
    }

    @SuppressWarnings("rawtypes")
    private BlockBodyParser getBlockParser(int blockType) {
        BlockBodyParser parser = null;
        switch (blockType) {

            case BLOCK_TYPE_STREAMINFO:
                parser = streamInfoParser;
                break;
            case BLOCK_TYPE_VORBIS_COMMENT:
                parser = commentParser;
                break;
            case BLOCK_TYPE_PICTURE:
                parser = pictureParser;
                break;
        }
        return parser;
    }

    public static int getBitrate(StreamInfo streamInfo, int streamLength) {
        return (int) ((streamLength * 8f) / (streamInfo.getDuration() * 1000f));
    }

    int getStreamOffset() {
        return streamOffset;
    }

    @Override
    public StreamInfoBlock parseStreamInfo(RandomAccessFile file) throws IOException {

        if (flac == null)
            parseTag(file);

        StreamInfoBlock streamInfo;
        int streamLength;
        int bitrate;

        streamInfo   = flac.getBlock(BLOCK_TYPE_STREAMINFO);
        streamLength = (int) (file.length() - streamOffset);
        bitrate      = getBitrate(streamInfo, streamLength);

        streamInfo.setBitrate(bitrate);
        return streamInfo;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public FLAC parseTag(RandomAccessFile file) throws IOException {

        if (flac != null) return flac;
        flac = new FLAC();

        byte[] magicBytes = new byte[4];
        file.readFully(magicBytes);

        String magic = new String(magicBytes);
        if (!magic.equals(MAGIC))
            throw new IllegalStateException("Invalid file signature");

        long length = file.length();
        int blockIndex = 0;
        while (file.getFilePointer() < length) {

            boolean isLastBlock;
            int blockType;
            int blockLength;

            byte blockHeader;
            byte[] lengthBytes;
            byte[] blockData;

            lengthBytes = new byte[3];
            blockHeader = file.readByte();
            isLastBlock = (blockHeader & 0x80) != 0;
            blockType   = blockHeader & 0x7f;

            if (!isValidBlockType(blockType))
                throw new IllegalStateException("Invalid block type " + blockType);

            if (blockIndex == 0 && blockType != BLOCK_TYPE_STREAMINFO)
                throw new IllegalStateException("First block should be STREAMINFO");

            file.readFully(lengthBytes);
            blockLength = IntegerUtils.toUInt24BE(lengthBytes);

            if (blockLength <= 0)
                throw new IllegalStateException(String.format("Invalid size for metadata block: %d\n", blockLength));

            if (blockType != BLOCK_TYPE_PADDING) {

                blockData = new byte[blockLength];
                file.readFully(blockData);
                BlockBodyParser parser = getBlockParser(blockType);

                flac.addBlock(parser != null ? parser.parse(blockData) :
                        new UnknownMetadataBlock(blockData, blockHeader));
            }
            if (isLastBlock) {
                streamOffset = (int) file.getFilePointer() + blockLength;
                break;
            }
            blockIndex++;
        }
        if (flac.getBlocks().isEmpty())
            throw new IllegalStateException("FLAC must contain one STREAMINFO block");
        return flac;
    }
}
