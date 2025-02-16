package com.jtagger.flac;

import com.jtagger.AbstractTagEditor;
import com.jtagger.AbstractTag;
import com.jtagger.utils.BytesIO;
import com.jtagger.utils.IntegerUtils;

import java.io.IOException;
import static com.jtagger.flac.AbstractMetadataBlock.*;
import static com.jtagger.utils.BytesIO.PADDING_MIN;

public class FlacTagEditor extends AbstractTagEditor<FLAC> {

    private FlacParser parser;
    private StreamInfoBlock sInfo;

    public FlacParser getParser() {
        return parser;
    }

    @Override
    protected void parseTag() throws IOException {
        parser = new FlacParser();
        tag    = parser.parseTag(file);
        sInfo  = (StreamInfoBlock) tag.getBlocks().get(0);
    }

    @Override
    public void removeTag() {
        tag.removeBlock(BLOCK_TYPE_VORBIS_COMMENT);
        tag.removeBlock(BLOCK_TYPE_PICTURE);
        tag.assemble();
    }

    @Override
    public void commit() throws IOException {

        super.commit();
        byte[] tagBuffer = tag.getBytes();

        int origSize = parser.getStreamOffset() - 4; // subtract fLaC signature
        int maxPad   = BytesIO.getPadding((int) file.length());
        int padding  = origSize - tagBuffer.length;

        file.seek(4);
        if ((padding - 4) >= PADDING_MIN && (padding - 4) <= maxPad) {
            padding -= 4;
            file.write(tagBuffer);
            file.write(BLOCK_TYPE_PADDING | 0x80);
            file.write(IntegerUtils.fromUInt24BE(padding));
            file.write(new byte[padding]);
            return;
        } else {
            padding = PADDING_MIN;
        }

        int newSize = tagBuffer.length + padding + 4; // +4 block header size
        int delta   = newSize - origSize;
        int from    = parser.getStreamOffset();
        int to      = from + delta;

        BytesIO.moveBlock(file, from, to, delta, (int) file.length() - from);
        BytesIO.writeBlock(file, tagBuffer, 4);

        file.write(BLOCK_TYPE_PADDING | 0x80);
        file.write(IntegerUtils.fromUInt24BE(padding));
        file.write(new byte[padding]);
    }

    @Override
    public void setTag(AbstractTag tag) {

        if (tag instanceof FLAC) {
            this.tag = (FLAC) tag;
            return;
        }

        FLAC flacTag = new FLAC();
        flacTag.addBlock(sInfo);
        convertTag(tag, this.tag);
    }
}
