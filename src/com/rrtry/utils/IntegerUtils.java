package com.rrtry.utils;

public class IntegerUtils {

    public static int toUInt32BE(byte[] bytes) {
        return  (bytes[0] << 24) |
                (bytes[1] & 0xFF) << 16 |
                (bytes[2] & 0xFF) << 8 |
                (bytes[3] & 0xFF);
    }

    public static int toUInt32LE(byte[] bytes) {
        return  (bytes[3] << 24) |
                (bytes[2] & 0xFF) << 16 |
                (bytes[1] & 0xFF) << 8 |
                (bytes[0] & 0xFF);
    }

    public static int toUInt24BE(byte[] bytes) {
        return  (bytes[0] & 0xFF) << 16 |
                (bytes[1] & 0xFF) << 8  |
                (bytes[2] & 0xFF);
    }

    public static byte[] fromUInt24BE(int in) {
        return new byte[] {
                (byte) (in >>> 16),
                (byte) (in >>> 8),
                (byte) in
        };
    }

    public static byte[] fromUInt32BE(int in) {
        return new byte[] {
                (byte) (in >>> 24),
                (byte) (in >>> 16),
                (byte) (in >>> 8),
                (byte) in
        };
    }

    public static byte[] fromUInt32LE(int in) {
        return new byte[] {
                (byte) in,
                (byte) (in >>> 8),
                (byte) (in >>> 16),
                (byte) (in >>> 24)
        };
    }

    public static short toUShort16BE(byte[] bytes) {

        short value = 0x00;

        value |= (bytes[0] & 0xFF) << 8;
        value |= (bytes[1] & 0xFF);

        return value;
    }
}
