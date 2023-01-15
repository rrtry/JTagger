package com.rrtry.ogg.vorbis;

import com.rrtry.ogg.CommentHeader;
import com.rrtry.ogg.OggPacket;
import com.rrtry.ogg.OggTagEditor;

import java.util.ArrayList;

public class OggVorbisTagEditor extends OggTagEditor {

    @Override
    protected String getFileMimeType() {
        return OGG_VORBIS_MIME_TYPE;
    }

    @Override
    protected CommentHeader getCommentHeader() {

        VorbisCommentHeader commentHeader = new VorbisCommentHeader();
        commentHeader.setVorbisComments(tag);
        commentHeader.assemble();

        return commentHeader;
    }

    @Override
    protected ArrayList<OggPacket> getPackets() {

        ArrayList<OggPacket> oggPackets = new ArrayList<>();

        OggPacket commentHeaderPacket = new OggPacket(getCommentHeader().getBytes());
        OggPacket setupHeaderPacket   = packets.get(2);

        oggPackets.add(commentHeaderPacket);
        oggPackets.add(setupHeaderPacket);

        return oggPackets;
    }
}
