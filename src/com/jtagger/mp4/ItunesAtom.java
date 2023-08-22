package com.jtagger.mp4;

abstract public class ItunesAtom <T> extends MP4Atom {

    public static final int TYPE_UTF8     = 0x000001;
    public static final int TYPE_UTF16    = 0x000002;
    public static final int TYPE_INTEGER  = 0x000015;
    public static final int TYPE_JPEG     = 0x00000D;
    public static final int TYPE_PNG      = 0x00000E;
    public static final int TYPE_BMP      = 0x00001B;
    public static final int TYPE_IMPLICIT = 0x000000;

    public ItunesAtom(String type, byte[] data) {
        super(type, data);
    }

    public ItunesAtom(String type) {
        super(type);
    }

    abstract void setAtomData(byte[] data);
    abstract public T getAtomData();
    abstract public int getAtomType();
    abstract public void setAtomData(T data);
}
