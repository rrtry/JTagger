package com.jtagger.mp4;

import com.jtagger.utils.IntegerUtils;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class NumberAtom extends ItunesAtom<Number> {

    private Number number;
    private byte length;

    NumberAtom(String type, byte[] atomData) {
        super(type, atomData);
    }

    public NumberAtom(String type) {
        super(type);
    }

    private byte[] getNumberBytes() {
        if (length == 1) return new byte[] { number.byteValue() };
        if (length == 2) return IntegerUtils.fromUInt16BE(number.shortValue());
        if (length == 3) return IntegerUtils.fromUInt24BE(number.intValue());
        if (length == 4) return IntegerUtils.fromUInt32BE(number.intValue());
        if (length == 8) return IntegerUtils.fromUInt64BE(number.longValue());
        throw new IllegalStateException("Invalid int length");
    }

    @Override
    public byte[] assemble() {

        int index          = 0;
        byte[] numberBytes = getNumberBytes();

        assert numberBytes != null;
        byte[] dataAtom   = new byte[8 + 8 + numberBytes.length];
        byte[] itunesAtom = new byte[dataAtom.length + 8];

        System.arraycopy(IntegerUtils.fromUInt32BE(dataAtom.length), 0, dataAtom, 0, 4); index += 4;
        System.arraycopy("data".getBytes(ISO_8859_1), 0, dataAtom, index, 4); index += 4;

        System.arraycopy(IntegerUtils.fromUInt32BE(TYPE_INTEGER), 0, dataAtom, index, 4); index += 4;
        System.arraycopy(IntegerUtils.fromUInt32BE(0x000000), 0, dataAtom, index, 4); index += 4;
        System.arraycopy(numberBytes, 0, dataAtom, index, numberBytes.length);

        System.arraycopy(IntegerUtils.fromUInt32BE(itunesAtom.length), 0, itunesAtom, 0, 4);
        System.arraycopy(getType().getBytes(ISO_8859_1), 0, itunesAtom, 4, 4);
        System.arraycopy(dataAtom, 0, itunesAtom, 8, dataAtom.length);

        this.data = itunesAtom;
        return itunesAtom;
    }

    @Override
    public Number getAtomData() {
        return number;
    }

    @Override
    public int getAtomType() {
        return ItunesAtom.TYPE_INTEGER;
    }

    @Override
    public void setAtomData(byte[] data) {
        switch (data.length) {
            case 1: number = data[0]; break;
            case 2: number = IntegerUtils.toUInt16BE(data); break;
            case 3: number = IntegerUtils.toUInt24BE(data); break;
            case 4: number = IntegerUtils.toUInt32BE(data); break;
            case 8: number = IntegerUtils.toUInt64BE(data); break;
        }
        length = (byte) data.length;
    }

    @Override
    public void setAtomData(Number num) {
        this.number = num;
    }

    public void setNumberLength(byte length) {
        this.length = length;
    }
}
