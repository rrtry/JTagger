package com.jtagger.mp4;

public class UnknownAtom extends ItunesAtom<byte[]> {

    private int atomType;

    public UnknownAtom(String type, byte[] data, int atomType) {
        super(type, data);
        this.atomType = atomType;
    }

    public UnknownAtom(String type) {
        super(type);
    }

    @Override
    public byte[] getAtomData() {
        return getData();
    }

    @Override
    public int getAtomType() {
        return atomType;
    }

    @Override
    public void setAtomData(byte[] data) {
        this.data = data;
    }
}
