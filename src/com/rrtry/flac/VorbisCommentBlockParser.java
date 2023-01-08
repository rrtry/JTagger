package com.rrtry.flac;

import com.rrtry.ogg.VorbisComments;
import com.rrtry.ogg.VorbisCommentsParser;

public class VorbisCommentBlockParser implements BlockBodyParser<VorbisCommentBlock> {

    @Override
    public VorbisCommentBlock parse(byte[] data) {

        VorbisCommentsParser parser           = new VorbisCommentsParser();
        VorbisCommentBlock vorbisCommentBlock = new VorbisCommentBlock();

        VorbisComments vorbisComments = parser.parse(data);
        vorbisCommentBlock.setVorbisComments(vorbisComments);

        return vorbisCommentBlock;
    }
}
