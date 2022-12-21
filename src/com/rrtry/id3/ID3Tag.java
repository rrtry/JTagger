package com.rrtry.id3;
import com.rrtry.Tag;

public interface ID3Tag extends Tag {

    byte getVersion();
    void setVersion(byte version);
}
