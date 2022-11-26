package com.rrtry;

public class ID3Integer {

    public static int getIntegerFromBytes(byte[] bytes) {
        return  (bytes[0] << 24) |
                (bytes[1] & 0xFF) << 16 |
                (bytes[2] & 0xFF) << 8 |
                (bytes[3] & 0xFF);
    }

    public static byte[] getBytesFromInteger(int in) {
        return new byte[] {
                (byte) (in >>> 24),
                (byte) (in >>> 16),
                (byte) (in >>> 8),
                (byte) in
        };
    }
}
