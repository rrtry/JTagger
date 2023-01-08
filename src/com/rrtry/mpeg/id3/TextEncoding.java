package com.rrtry.mpeg.id3;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.rrtry.mpeg.id3.ID3V2Tag.ID3V2_3;

public class TextEncoding {

    public static final byte ENCODING_LATIN_1 = 0x00;
    public static final byte ENCODING_UTF_16 = 0x01;
    public static final byte ENCODING_UTF_16_BE = 0x02;
    public static final byte ENCODING_UTF_8 = 0x03;

    public static final int UTF_16_BOM_LENGTH = 2;

    public static byte getAppropriateEncoding(byte version) {
        if (version == ID3V2_3) return ENCODING_UTF_16;
        return ENCODING_UTF_8;
    }

    public static byte[] getStringBytes(String text, byte encoding) {

        byte[] bytes;

        switch (encoding) {
            case ENCODING_UTF_16: bytes = appendNullTerminator(prependByteOrderMark(text), ENCODING_UTF_16); break;
            case ENCODING_UTF_8: bytes = appendNullTerminator(text.getBytes(StandardCharsets.UTF_8), ENCODING_UTF_8); break;
            case ENCODING_UTF_16_BE: bytes = appendNullTerminator(text.getBytes(StandardCharsets.UTF_16BE), ENCODING_UTF_16_BE); break;
            case ENCODING_LATIN_1: bytes = appendNullTerminator(text.getBytes(StandardCharsets.ISO_8859_1), ENCODING_LATIN_1); break;
            default: throw new IllegalArgumentException("Invalid encoding: " + encoding);
        }
        return bytes;
    }

    public static byte[] prependByteOrderMark(String s) {

        byte[] bytes = s.getBytes(StandardCharsets.UTF_16LE);
        byte[] bom = new byte[bytes.length + 2];
        bom[0] = (byte) 0xFF; bom[1] = (byte) 0xFE;

        System.arraycopy(bytes, 0, bom, 2, bytes.length);
        return bom;
    }

    public static byte[] appendNullTerminator(byte[] bytes, byte encoding) {
        int terminatorLength = 1;
        if (encoding == ENCODING_UTF_16 || encoding == ENCODING_UTF_16_BE) {
            terminatorLength++;
        }
        return Arrays.copyOf(bytes, bytes.length + terminatorLength);
    }

    public static boolean hasByteOrderMark(byte[] data, int from) {

        byte[] utf16Little = new byte[] { (byte) 0xFF, (byte) 0xFE };
        byte[] utf16Big = new byte[] { (byte) 0xFE, (byte) 0xFF};

        byte[] utf16Buffer = Arrays.copyOfRange(data, from, from + UTF_16_BOM_LENGTH);

        return Arrays.equals(utf16Buffer, utf16Big) ||
                Arrays.equals(utf16Buffer, utf16Little);
    }

    public static boolean isValidEncodingByte(byte encoding) {
        return encoding == ENCODING_LATIN_1 ||
                encoding == ENCODING_UTF_16 ||
                encoding == ENCODING_UTF_16_BE ||
                encoding == ENCODING_UTF_8;
    }

    public static boolean isUTF16(Charset charset) {
        return charset.equals(StandardCharsets.UTF_16) ||
                charset.equals(StandardCharsets.UTF_16BE);
    }

    public static Charset getCharset(byte b) {

        Charset charset = StandardCharsets.ISO_8859_1;

        switch (b) {
            case ENCODING_UTF_16: charset = StandardCharsets.UTF_16; break;
            case ENCODING_UTF_16_BE: charset = StandardCharsets.UTF_16BE; break;
            case ENCODING_UTF_8: charset = StandardCharsets.UTF_8; break;
        }
        return charset;
    }
}
