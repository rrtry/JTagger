package com.jtagger.ogg.flac;

import com.jtagger.flac.VorbisCommentBlock;
import com.jtagger.ogg.*;

import static com.jtagger.flac.AbstractMetadataBlock.BLOCK_TYPE_VORBIS_COMMENT;
import static com.jtagger.ogg.flac.OggFlacParser.FLAC_BLOCK_HEADER_OFFSET;
import static com.jtagger.ogg.flac.OggFlacParser.HEADER_PACKETS_OFFSET;
import static com.jtagger.utils.IntegerUtils.fromUInt16BE;

public class OggFlacTagEditor extends OggTagEditor {

    @Override
    protected void setHeaderPackets() {

        VorbisCommentBlock commentBlock = new VorbisCommentBlock();
        commentBlock.setVorbisComments(tag);
        commentBlock.assemble();

        OggPacket packet;
        int index = -1;
        for (int i = 1; i < packets.size(); i++) {
            packet = packets.get(i);
            if ((packet.getBuffer()[0] & 0x7F) == BLOCK_TYPE_VORBIS_COMMENT) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            packets.add(1, new OggPacket(commentBlock.getBytes()));
        } else {
            packets.set(1, new OggPacket(commentBlock.getBytes()));
        }
        byte[] page = pages.get(0).getBuffer();
        System.arraycopy(fromUInt16BE(packets.size() - 1), 0, page, HEADER_PACKETS_OFFSET, Short.BYTES);
        page[FLAC_BLOCK_HEADER_OFFSET] &= ~0x80;
    }

    /*
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
    } */
}
