package com.rrtry.ogg;

import com.rrtry.Component;
import com.rrtry.ogg.vorbis.VorbisComments;

public interface CommentHeader extends Component {

    void setVorbisComments(VorbisComments vorbisComments);
    VorbisComments getVorbisComments();
}
