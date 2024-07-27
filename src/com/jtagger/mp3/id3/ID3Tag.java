package com.jtagger.mp3.id3;
import com.jtagger.AbstractTag;

public abstract class ID3Tag extends AbstractTag {

    abstract public byte getVersion();
    abstract public void setVersion(byte version);
}
