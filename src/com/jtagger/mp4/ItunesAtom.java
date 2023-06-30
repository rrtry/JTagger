package com.jtagger.mp4;

public interface ItunesAtom <T> {

    int TYPE_UTF8     = 0x000001;
    int TYPE_INTEGER  = 0x000015;
    int TYPE_JPEG     = 0x00000D;
    int TYPE_IMPLICIT = 0x000000;

    T getAtomData();
    void setAtomData(byte[] data);
    void setAtomData(T data);
}
