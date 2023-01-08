package com.rrtry.mpeg.id3;

import com.rrtry.utils.IntegerUtils;

public class ID3SynchSafeInteger {

    public static int fromSynchSafeIntegerBytes(byte[] bytes) {
        return fromSynchSafeInteger(IntegerUtils.toUInt32BE(bytes));
    }

    public static int toSynchSafeInteger(int in) {

        int out = 0x7F, mask = 0x7F;

        while ((mask ^ 0x7FFFFFFF) != 0) {
            out = in & ~mask;
            out <<= 1;
            out |= in & mask;
            mask = ((mask + 1) << 8) - 1;
            in = out;
        }
        return out;
    }

    public static int fromSynchSafeInteger(int in) {

        int out = 0, mask = 0x7F000000;

        while (mask != 0) {
            out >>= 1;
            out |= in & mask;
            mask >>= 8;
        }
        return out;
    }
}
