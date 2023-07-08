package com.jtagger.mp4;

import com.jtagger.utils.IntegerUtils;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public class FreeFormAtom extends MP4Atom implements ItunesAtom<String> {

    private String text;

    public FreeFormAtom(String mean, String name, byte[] data) {
        super("----:" + mean + ":" + name, data);
    }

    @Override
    public byte[] assemble(byte version) {

        int index        = 0;
        byte[] textBytes = text.getBytes(UTF_8);
        byte[] typeBytes = getType().getBytes(ISO_8859_1);

        byte[] freeFormAtom = new byte[4 + typeBytes.length + 8 + 4 + textBytes.length];
        System.arraycopy(IntegerUtils.fromUInt32BE(freeFormAtom.length), 0, freeFormAtom, index, 4); index += 4;
        System.arraycopy(typeBytes, 0, freeFormAtom, index, typeBytes.length); index += typeBytes.length;

        System.arraycopy(IntegerUtils.fromUInt32BE(textBytes.length + 8 + 4), 0, freeFormAtom, index, 4); index += 4;
        System.arraycopy("data".getBytes(ISO_8859_1), 0, freeFormAtom, index, 4); index += 4;
        System.arraycopy(IntegerUtils.fromUInt32BE(TYPE_UTF8), 0, freeFormAtom, index, 4); index += 4;
        System.arraycopy(textBytes, 0, freeFormAtom, index, textBytes.length);

        data = freeFormAtom;
        return freeFormAtom;
    }

    @Override
    public String getAtomData() {
        return text;
    }

    @Override
    public int getAtomType() {
        return ItunesAtom.TYPE_UTF8;
    }

    @Override
    public void setAtomData(byte[] data) {
        this.text = new String(data, UTF_8);
    }

    @Override
    public void setAtomData(String data) {
        this.text = data;
    }
}
