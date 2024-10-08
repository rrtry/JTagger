package com.jtagger.ogg.opus;

import com.jtagger.ogg.OggPacket;
import com.jtagger.ogg.OggTagEditor;

public class OggOpusTagEditor extends OggTagEditor {

    @Override
    protected void setHeaderPackets() {
        OpusCommentHeader commentHeader = new OpusCommentHeader();
        commentHeader.setVorbisComments(tag);
        commentHeader.assemble();
        packets.set(1, new OggPacket(commentHeader.getBytes()));
    }
}
