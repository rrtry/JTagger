package com.jtagger.ogg.opus;

import com.jtagger.MediaFile;
import com.jtagger.ogg.CommentHeader;
import com.jtagger.ogg.OggPacket;
import com.jtagger.ogg.OggTagEditor;

import java.util.ArrayList;

public class OggOpusTagEditor extends OggTagEditor {

    @Override
    protected String getFileMimeType() {
        return MediaFile.FileContentTypeDetector.OGG_OPUS_MIME_TYPE;
    }

    @Override
    protected CommentHeader getCommentHeader() {

        OpusCommentHeader commentHeader = new OpusCommentHeader();
        commentHeader.setVorbisComments(tag);
        commentHeader.assemble();

        return commentHeader;
    }

    @Override
    protected ArrayList<OggPacket> getHeaderPackets() {

        ArrayList<OggPacket> oggPackets = new ArrayList<>();
        oggPackets.add(new OggPacket(getCommentHeader().getBytes()));

        return oggPackets;
    }
}
