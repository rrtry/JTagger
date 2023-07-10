package com.jtagger.flac;

import com.jtagger.StreamInfoParser;
import com.jtagger.TagParser;
import com.jtagger.utils.IntegerUtils;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.jtagger.flac.FlacTag.*;
import static com.jtagger.flac.AbstractMetadataBlock.*;

public class FlacParser implements TagParser<FlacTag>, StreamInfoParser<StreamInfoBlock> {

    private FlacTag tag;

    @Override
    public StreamInfoBlock parseStreamInfo(RandomAccessFile file) throws IOException {

        if (tag == null) {
            parseTag(file);
        }

        int streamLength;
        int bitRate;

        StreamInfoBlock streamInfo = tag.getBlock(BLOCK_TYPE_STREAMINFO);

        streamLength = (int) (file.length() - tag.getBytes().length);
        bitRate      = (int) ((streamLength * 8f) / (streamInfo.getDuration() * 1000f));
        streamInfo.setBitrate(bitRate);

        return streamInfo;
    }

    @Override
    public FlacTag parseTag(RandomAccessFile file) {
        try {

            if (this.tag != null) return tag;
            FlacTag tag = new FlacTag();

            byte[] magicBytes = new byte[MAGIC.length()];
            file.read(magicBytes, 0, magicBytes.length);

            String magic = new String(magicBytes);
            if (!magic.equals(MAGIC)) return null;

            file.seek(magicBytes.length);
            while (file.getFilePointer() < file.length()) {

                boolean isLastBlock;

                int blockType;
                int blockLength;

                byte headerByte;
                byte[] lengthBytes = new byte[3];

                headerByte  = file.readByte();
                isLastBlock = (headerByte & 0x80) != 0;
                blockType   = (headerByte & 0x7f);

                file.read(lengthBytes, 0, lengthBytes.length);
                blockLength = IntegerUtils.toUInt24BE(lengthBytes);

                byte[] blockData = new byte[blockLength];
                file.read(blockData, 0, blockData.length);

                BlockBodyParser parser = null;

                if (blockType == BLOCK_TYPE_VORBIS_COMMENT) parser = new VorbisCommentBlockParser();
                if (blockType == BLOCK_TYPE_PICTURE)        parser = new PictureBlockParser();
                if (blockType == BLOCK_TYPE_STREAMINFO)     parser = new StreamInfoBlockParser();

                if (parser != null) tag.addBlock(parser.parse(blockData));
                else tag.addBlock(new UnknownMetadataBlock(blockData, headerByte));

                if (isLastBlock) break;
            }

            tag.assemble();
            this.tag = tag;
            return tag;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
