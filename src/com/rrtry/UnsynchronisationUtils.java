package com.rrtry;

import java.util.Arrays;

public class UnsynchronisationUtils {

    public static byte[] toUnsynch(byte[] data) {

        byte[] unsynchFrame = new byte[data.length * 2];

        int x = 0;
        int n = 0;

        for (int i = 0; i < data.length; i++) {

            if (i + 1 < data.length) {

                int v = ((int) data[i]) & 0xFF;
                int c = ((int) data[i + 1]) & 0xFF;

                if (v == 0xFF && (c == 0x00 || c == 0xE0)) {
                    unsynchFrame[x] = data[i]; unsynchFrame[++x] = 0x00; unsynchFrame[++x] = data[i + 1];
                    n++; i++;
                } else {
                    unsynchFrame[x] = data[i];
                }
            } else {
                unsynchFrame[x] = data[i];
            }
            x++;
        }
        return Arrays.copyOf(unsynchFrame, data.length + n);
    }

    public static byte[] fromUnsynch(byte[] data) {

        byte[] synchFrame = new byte[data.length];

        int x = 0;
        int n = 0;

        for (int i = 0; i < data.length; i++) {

            if (i + 2 < data.length) {

                int v = ((int) data[i]) & 0xFF;
                int c = ((int) data[i + 2]) & 0xFF;

                if (v == 0xFF && data[i + 1] == 0x00 && (c == 0xE0 || c == 0x00)) {
                    synchFrame[x] = data[i]; synchFrame[x + 1] = data[i + 2];
                    i += 2; x += 2; n++;
                    continue;
                } else {
                    synchFrame[x] = data[i];
                }
            } else {
                synchFrame[x] = data[i];
            }
            x++;
        }
        return Arrays.copyOf(synchFrame, synchFrame.length - n);
    }
}
