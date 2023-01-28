package com.rrtry.mpeg.id3;
import com.rrtry.AbstractTag;

public abstract class ID3Tag extends AbstractTag {

    abstract byte getVersion();
    abstract void setVersion(byte version);
}
