package com.jtagger.ogg.vorbis;

import com.jtagger.MediaFile;
import com.jtagger.ogg.CommentHeader;
import com.jtagger.ogg.OggPacket;
import com.jtagger.ogg.OggTagEditor;

import java.util.ArrayList;

public class OggVorbisTagEditor extends OggTagEditor {

    @Override
    protected String getFileMimeType() {
        return MediaFile.FileContentTypeDetector.OGG_VORBIS_MIME_TYPE;
    }

    @Override
    protected CommentHeader getCommentHeader() {

        VorbisCommentHeader commentHeader = new VorbisCommentHeader();
        commentHeader.setVorbisComments(tag);
        commentHeader.assemble();

        return commentHeader;
    }

    @Override
    protected ArrayList<OggPacket> getHeaderPackets() {

        ArrayList<OggPacket> oggPackets = new ArrayList<>();

        OggPacket commentHeaderPacket = new OggPacket(getCommentHeader().getBytes());
        OggPacket setupHeaderPacket   = packets.get(2);

        oggPackets.add(commentHeaderPacket);
        oggPackets.add(setupHeaderPacket);

        return oggPackets;
    }
}
