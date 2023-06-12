package com.rrtry.utils;

public class IntegerUtils {

    @SuppressWarnings("all")
    public static long toUInt64LE(byte[] b) {
        return  (b[7] << 56) |
                (b[6] & 0xFF) << 48 |
                (b[5] & 0xFF) << 40 |
                (b[4] & 0xFF) << 32 |
                (b[3] & 0xFF) << 24 |
                (b[2] & 0xFF) << 16 |
                (b[1] & 0xFF) << 8  |
                (b[0] & 0xFF);
    }

    public static byte[] fromUInt64LE(long in) {
        return new byte[] {
                (byte) in,
                (byte) (in >>> 8),
                (byte) (in >>> 16),
                (byte) (in >>> 24),
                (byte) (in >>> 32),
                (byte) (in >>> 40),
                (byte) (in >>> 48),
                (byte) (in >>> 56)
        };
    }

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

    public static byte[] fromUInt16BE(long in) {
        return new byte[] {
                (byte) (in >>> 8),
                (byte) in
        };
    }

    public static short toUInt16LE(byte[] bytes) {
        return (short) ((bytes[1] & 0xFF << 8) | (bytes[0] & 0xFF));
    }

    public static short toUInt16BE(byte[] bytes) {
        return (short) ((bytes[0] & 0xFF << 8) | (bytes[1] & 0xFF));
    }
}
