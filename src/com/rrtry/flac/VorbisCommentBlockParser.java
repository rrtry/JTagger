package com.rrtry.flac;

import com.rrtry.ogg.vorbis.VorbisComments;
import com.rrtry.ogg.vorbis.VorbisCommentsParser;

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
