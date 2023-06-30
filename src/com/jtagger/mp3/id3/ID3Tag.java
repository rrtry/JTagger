package com.jtagger.mp3.id3;
import com.jtagger.AbstractTag;

public abstract class ID3Tag extends AbstractTag {

    abstract byte getVersion();
    abstract void setVersion(byte version);
}
