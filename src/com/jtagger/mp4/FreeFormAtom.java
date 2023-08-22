package com.jtagger.mp4;

import java.util.Arrays;

import static com.jtagger.mp4.TextAtom.getCharset;
import static com.jtagger.utils.IntegerUtils.fromUInt32BE;
import static java.nio.charset.StandardCharsets.*;

public class FreeFormAtom extends ItunesAtom<String> {

    private final int atomType;
    private final int dataOffset;

    private String text;
    private final String mean;
    private final String name;

    FreeFormAtom(String mean, String name, byte[] data, int atomType, int dataOffset) {
        super("----:" + mean + ":" + name, data);
        this.atomType   = atomType;
        this.mean       = mean;
        this.name       = name;
        this.dataOffset = dataOffset;
    }

    public FreeFormAtom(String mean, String name) {
        super("----:" + mean + ":" + name);
        this.atomType   = TYPE_UTF8;
        this.mean       = mean;
        this.name       = name;
        this.dataOffset = -1;
    }

    @Override
    public byte[] assemble(byte version) {

        if (mean.isEmpty()) throw new IllegalArgumentException("FreeFormAtom: 'mean' cannot be empty");
        if (name.isEmpty()) throw new IllegalArgumentException("FreeFormAtom: 'name' cannot be empty");

        if (dataOffset == -1) {

            byte[] type = "----".getBytes(ISO_8859_1);
            byte[] mean = "mean".getBytes(ISO_8859_1);
            byte[] name = "name".getBytes(ISO_8859_1);
            byte[] data = "data".getBytes(ISO_8859_1);

            byte[] dataBody = text.getBytes(getCharset(getType(), atomType));
            byte[] meanBody = this.mean.getBytes(UTF_8);
            byte[] nameBody = this.name.getBytes(UTF_8);

            byte[] atom       = new byte[8 + 12 + meanBody.length + 12 + nameBody.length + 16 + dataBody.length];
            byte[] atomLength = fromUInt32BE(atom.length);
            byte[] meanLength = fromUInt32BE(meanBody.length + 12);
            byte[] nameLength = fromUInt32BE(nameBody.length + 12);
            byte[] dataLength = fromUInt32BE(16 + dataBody.length);

            byte[] atomType = fromUInt32BE(getAtomType());
            byte[] flags    = fromUInt32BE(0x0);

            int i = 0;
            System.arraycopy(atomLength, 0, atom, i, 4); i += 4;
            System.arraycopy(type,       0, atom, i, 4); i += 4;
            System.arraycopy(meanLength, 0, atom, i, 4); i += 4;
            System.arraycopy(mean,       0, atom, i, 4); i += 4;
            System.arraycopy(flags,      0, atom, i, 4); i += 4;
            System.arraycopy(meanBody,   0, atom, i, meanBody.length);
            i += meanBody.length;

            System.arraycopy(nameLength, 0, atom, i, 4); i += 4;
            System.arraycopy(name,       0, atom, i, 4); i += 4;
            System.arraycopy(flags,      0, atom, i, 4); i += 4;
            System.arraycopy(nameBody,   0, atom, i, nameBody.length);
            i += nameBody.length;

            System.arraycopy(dataLength, 0, atom, i, 4); i += 4;
            System.arraycopy(data,       0, atom, i, 4); i += 4;
            System.arraycopy(atomType,   0, atom, i, 4); i += 4;
            System.arraycopy(flags,      0, atom, i, 4); i += 4;
            System.arraycopy(dataBody,   0, atom, i, dataBody.length);

            this.data = atom;
            return atom;
        }

        byte[] textBytes = text.getBytes(getCharset(getType(), atomType));
        byte[] infoAtom  = Arrays.copyOfRange(data, 4, dataOffset);
        byte[] atom      = new byte[dataOffset + 16 + textBytes.length];

        final int dataLength      = 16 + textBytes.length;
        final int atomTypeOffset  = dataOffset + 4;
        final int dataTypeOffset  = dataOffset + 8;
        final int dataFlagsOffset = dataOffset + 12;
        final int dataBodyOffset  = dataOffset + 16;

        System.arraycopy(fromUInt32BE(atom.length), 0, atom, 0, 4);
        System.arraycopy(infoAtom, 0, atom, 4, infoAtom.length);
        System.arraycopy(fromUInt32BE(dataLength), 0, atom, dataOffset, 4);
        System.arraycopy("data".getBytes(ISO_8859_1), 0, atom, atomTypeOffset, 4);
        System.arraycopy(fromUInt32BE(atomType), 0, atom, dataTypeOffset, 4);
        System.arraycopy(fromUInt32BE(0x0), 0, atom, dataFlagsOffset, 4);
        System.arraycopy(textBytes, 0, atom, dataBodyOffset, textBytes.length);

        this.data = atom;
        return atom;
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
    public void setAtomData(byte[] data) {
        this.text = new String(data, getCharset(getType(), atomType));
    }

    @Override
    public void setAtomData(String data) {
        this.text = data;
    }

    public String getMean() {
        return mean;
    }

    public String getName() {
        return name;
    }
}
