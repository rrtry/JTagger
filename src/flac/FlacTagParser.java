package flac;

import com.rrtry.TagParser;
import utils.IntegerUtils;
import java.io.IOException;
import java.io.RandomAccessFile;

import static flac.FlacTag.*;
import static flac.AbstractMetadataBlock.*;

public class FlacTagParser implements TagParser<FlacTag> {

    @Override
    public FlacTag parse(RandomAccessFile file) throws IOException {

        FlacTag tag = new FlacTag();

        byte[] magicBytes = new byte[MAGIC.length()];
        file.read(magicBytes, 0, magicBytes.length);

        String magic = new String(magicBytes);
        if (!magic.equals(MAGIC)) return null;

        file.seek(magicBytes.length);

        byte block = 0;
        while (block < NUMBER_OF_BLOCKS) {

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

            block++;
            if (isLastBlock) break;
        }
        tag.assemble();
        return tag;
    }
}
