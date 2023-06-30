package com.jtagger.ogg;

import com.jtagger.Component;
import com.jtagger.ogg.vorbis.VorbisComments;

public interface CommentHeader extends Component {

    void setVorbisComments(VorbisComments vorbisComments);
    VorbisComments getVorbisComments();
}
