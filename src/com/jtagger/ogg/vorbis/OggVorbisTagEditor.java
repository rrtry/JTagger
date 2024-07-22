package com.jtagger.ogg.vorbis;

import com.jtagger.ogg.OggPacket;
import com.jtagger.ogg.OggTagEditor;

public class OggVorbisTagEditor extends OggTagEditor {

    @Override
    protected void setHeaderPackets() {
        VorbisCommentHeader commentHeader = new VorbisCommentHeader();
        commentHeader.setVorbisComments(tag);
        commentHeader.assemble();
        packets.set(1, new OggPacket(commentHeader.getBytes()));
    }
}
