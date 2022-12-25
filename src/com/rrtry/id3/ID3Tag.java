package com.rrtry.id3;
import com.rrtry.Tag;

public abstract class ID3Tag extends Tag {

    abstract byte getVersion();
    abstract void setVersion(byte version);
}
