package com.jtagger.flac;

import com.jtagger.ogg.vorbis.VorbisComments;
import com.jtagger.ogg.vorbis.VorbisCommentsParser;

public class VorbisCommentBlockParser implements BlockBodyParser<VorbisCommentBlock> {

    @Override
    public VorbisCommentBlock parse(byte[] data) {

        VorbisCommentsParser parser           = new VorbisCommentsParser();
        VorbisCommentBlock vorbisCommentBlock = new VorbisCommentBlock();

        VorbisComments vorbisComments = parser.parse(data, false);
        vorbisCommentBlock.setVorbisComments(vorbisComments);

        return vorbisCommentBlock;
    }
}
