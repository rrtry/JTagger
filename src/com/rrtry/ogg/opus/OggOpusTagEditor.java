package com.rrtry.ogg.opus;

import com.rrtry.ogg.CommentHeader;
import com.rrtry.ogg.OggPacket;
import com.rrtry.ogg.OggTagEditor;
import com.rrtry.utils.FileContentTypeDetector;
import java.util.ArrayList;

public class OggOpusTagEditor extends OggTagEditor {

    @Override
    protected String getFileMimeType() {
        return FileContentTypeDetector.OGG_OPUS_MIME_TYPE;
    }

    @Override
    protected CommentHeader getCommentHeader() {

        OpusCommentHeader commentHeader = new OpusCommentHeader();
        commentHeader.setVorbisComments(tag);
        commentHeader.assemble();

        return commentHeader;
    }

    @Override
    protected ArrayList<OggPacket> getPackets() {

        ArrayList<OggPacket> oggPackets = new ArrayList<>();
        oggPackets.add(new OggPacket(getCommentHeader().getBytes()));

        return oggPackets;
    }
}
