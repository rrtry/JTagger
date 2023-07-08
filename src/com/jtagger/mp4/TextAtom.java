package com.jtagger.mp4;

import com.jtagger.utils.IntegerUtils;

import static java.nio.charset.StandardCharsets.*;

public class TextAtom extends MP4Atom implements ItunesAtom<String> {

    private String text;
    private int atomType = ItunesAtom.TYPE_UTF8;

    public TextAtom(String type, byte[] data, int atomType) {
        super(type, data);
        this.atomType = atomType;
    }

    public TextAtom(String type) {
        super(type);
    }

    @Override
    public byte[] assemble(byte version) {

        int index        = 0;
        byte[] textBytes = text.getBytes(atomType == TYPE_UTF8 ? UTF_8 : UTF_16BE);

        byte[] dataAtom   = new byte[8 + 8 + textBytes.length];
        byte[] itunesAtom = new byte[dataAtom.length + 8];

        System.arraycopy(IntegerUtils.fromUInt32BE(dataAtom.length), 0, dataAtom, 0, 4); index += 4;
        System.arraycopy("data".getBytes(ISO_8859_1), 0, dataAtom, index, 4); index += 4;

        System.arraycopy(IntegerUtils.fromUInt32BE(ItunesAtom.TYPE_UTF8), 0, dataAtom, index, 4); index += 4;
        System.arraycopy(IntegerUtils.fromUInt32BE(0x000000), 0, dataAtom, index, 4); index += 4;
        System.arraycopy(textBytes, 0, dataAtom, index, textBytes.length);

        System.arraycopy(IntegerUtils.fromUInt32BE(itunesAtom.length), 0, itunesAtom, 0, 4);
        System.arraycopy(getType().getBytes(ISO_8859_1), 0, itunesAtom, 4, 4);
        System.arraycopy(dataAtom, 0, itunesAtom, 8, dataAtom.length);

        this.data = itunesAtom;
        return itunesAtom;
    }

    @Override
    public String getAtomData() {
        return text;
    }

    @Override
    public int getAtomType() {
        return atomType;
    }

    @Override
    public void setAtomData(byte[] stringBytes) {
        this.text = new String(stringBytes, UTF_8);
    }

    @Override
    public void setAtomData(String string) {
        this.text = string;
    }
}
