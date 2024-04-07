package com.jtagger.ogg.flac;

import com.jtagger.flac.VorbisCommentBlock;
import com.jtagger.ogg.*;

import java.io.IOException;
import java.util.ArrayList;

import static com.jtagger.ogg.flac.OggFlacParser.FLAC_BLOCK_HEADER_OFFSET;
import static com.jtagger.ogg.flac.OggFlacParser.HEADER_PACKETS_OFFSET;
import static com.jtagger.utils.IntegerUtils.fromUInt16BE;

public class OggFlacTagEditor extends OggTagEditor {

    @Override
    public void commit() throws IOException {
        byte[] page = pages.get(0).getData();
        System.arraycopy(fromUInt16BE(1), 0, page, HEADER_PACKETS_OFFSET, Short.BYTES);
        page[FLAC_BLOCK_HEADER_OFFSET] &= ~0x80;
        super.commit();
    }

    @Override
    protected CommentHeader getCommentHeader() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected ArrayList<OggPacket> getHeaderPackets() {

        ArrayList<OggPacket> packets    = new ArrayList<>();
        VorbisCommentBlock commentBlock = new VorbisCommentBlock();

        commentBlock.setVorbisComments(tag);
        commentBlock.setBlockLast(true);
        commentBlock.assemble();

        packets.add(new OggPacket(commentBlock.getBytes()));
        return packets;
    }
}
