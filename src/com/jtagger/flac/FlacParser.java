package com.jtagger.flac;

import com.jtagger.StreamInfo;
import com.jtagger.StreamInfoParser;
import com.jtagger.TagParser;
import com.jtagger.utils.IntegerUtils;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.jtagger.flac.FlacTag.*;
import static com.jtagger.flac.AbstractMetadataBlock.*;

public class FlacParser implements TagParser<FlacTag>, StreamInfoParser<StreamInfoBlock> {

    private FlacTag tag;

    public static boolean isValidBlockType(int blockType) {
        return blockType >= 0x00 && blockType <= 0x7E;
    }

    @SuppressWarnings("rawtypes")
    public static BlockBodyParser getBlockParser(int blockType) {
        if (blockType == BLOCK_TYPE_VORBIS_COMMENT) return new VorbisCommentBlockParser();
        if (blockType == BLOCK_TYPE_PICTURE)        return new PictureBlockParser();
        if (blockType == BLOCK_TYPE_STREAMINFO)     return new StreamInfoBlockParser();
        return null;
    }

    public static int getBitrate(StreamInfo streamInfo, int streamLength) {
        return (int) ((streamLength * 8f) / (streamInfo.getDuration() * 1000f));
    }

    @Override
    public StreamInfoBlock parseStreamInfo(RandomAccessFile file) throws IOException {

        if (tag == null) {
            parseTag(file);
        }

        StreamInfoBlock streamInfo;
        int streamLength;
        int bitRate;

        streamInfo   = tag.getBlock(BLOCK_TYPE_STREAMINFO);
        streamLength = (int) (file.length() - tag.getBytes().length);
        bitRate      = getBitrate(streamInfo, streamLength);

        streamInfo.setBitrate(bitRate);
        return streamInfo;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public FlacTag parseTag(RandomAccessFile file) throws IOException {

        if (this.tag != null) return tag;
        FlacTag tag = new FlacTag();

        byte[] magicBytes = new byte[MAGIC.length()];
        file.read(magicBytes, 0, magicBytes.length);

        String magic = new String(magicBytes);
        if (!magic.equals(MAGIC)) throw new IllegalStateException("Invalid file signature");

        file.seek(magicBytes.length);
        while (file.getFilePointer() < file.length()) {

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

            if (!isValidBlockType(blockType)) {
                throw new IllegalStateException("Invalid block type " + blockType);
            }

            file.read(lengthBytes, 0, lengthBytes.length);
            blockLength = IntegerUtils.toUInt24BE(lengthBytes);
            blockData   = new byte[blockLength];

            file.read(blockData, 0, blockData.length);
            BlockBodyParser parser = getBlockParser(blockType);

            if (parser != null) {
                tag.addBlock(parser.parse(blockData));
            } else {
                tag.addBlock(new UnknownMetadataBlock(blockData, blockHeader));
            }
            if (isLastBlock) break;
        }

        tag.assemble();
        this.tag = tag;
        return tag;
    }
}
