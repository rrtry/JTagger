package com.jtagger.mp4;

import com.jtagger.utils.IntegerUtils;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class StcoAtom extends MP4Atom {

    private int[] offsets;

    public StcoAtom(String type, byte[] data) {
        super(type, data);
    }

    @Override
    public byte[] assemble(byte version) {

        byte[] stco = new byte[8 + 1 + 3 + 4 + offsets.length * 4];
        stco[8] = 0x0;

        System.arraycopy(IntegerUtils.fromUInt32BE(stco.length), 0, stco, 0, 4);
        System.arraycopy("stco".getBytes(ISO_8859_1), 0, stco, 4, 4);
        System.arraycopy(IntegerUtils.fromUInt24BE(0x0), 0, stco, 9, 3);
        System.arraycopy(IntegerUtils.fromUInt32BE(offsets.length), 0, stco, 12, 4);

        int j = 16;
        for (int offset : offsets) {
            System.arraycopy(IntegerUtils.fromUInt32BE(offset), 0, stco, j, 4);
            j += 4;
        }

        this.data = stco;
        return stco;
    }

    public int[] getOffsets() {
        return offsets;
    }

    public void setOffsets(int[] offsets) {
        this.offsets = offsets;
    }

    public void updateOffsets(int offset) {
        for (int i = 0; i < offsets.length; i++) {
            offsets[i] += offset;
        }
    }
}
