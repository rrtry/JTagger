package com.jtagger.mp4;

public interface ItunesAtom <T> {

    int TYPE_UTF8     = 0x000001;
    int TYPE_UTF16    = 0x000002;
    int TYPE_INTEGER  = 0x000015;
    int TYPE_JPEG     = 0x00000D;
    int TYPE_PNG      = 0x00000E;
    int TYPE_BMP      = 0x00001B;
    int TYPE_IMPLICIT = 0x000000;

    T getAtomData();
    int getAtomType();
    void setAtomData(byte[] data);
    void setAtomData(T data);
}
