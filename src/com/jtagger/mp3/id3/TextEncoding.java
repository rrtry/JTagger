package com.jtagger.mp3.id3;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.jtagger.mp3.id3.ID3V2Tag.ID3V2_3;
import static com.jtagger.mp3.id3.ID3V2Tag.ID3V2_4;

public class TextEncoding {

    public static byte[] BIG_BOM    = new byte[] { (byte) 0xFE, (byte) 0xFF };
    public static byte[] LITTLE_BOM = new byte[] { (byte) 0xFF, (byte) 0xFE };

    public static final byte ENCODING_LATIN_1   = 0x00;
    public static final byte ENCODING_UTF_16    = 0x01;
    public static final byte ENCODING_UTF_16_BE = 0x02;
    public static final byte ENCODING_UTF_8     = 0x03;

    public static final int UTF_16_BOM_LENGTH = 2;

    public static byte getEncodingForVersion(byte version) {
        if (version == ID3V2_3) return ENCODING_UTF_16;
        if (version == ID3V2_4) return ENCODING_UTF_8;
        throw new IllegalArgumentException("Invalid version: " + version);
    }

    public static byte[] getStringBytes(String text, byte encoding) {
        byte[] bytes;
        switch (encoding) {
            case ENCODING_UTF_16:    bytes = appendNullTerminator(prependByteOrderMark(text), ENCODING_UTF_16); break;
            case ENCODING_UTF_8:     bytes = appendNullTerminator(text.getBytes(StandardCharsets.UTF_8), ENCODING_UTF_8); break;
            case ENCODING_UTF_16_BE: bytes = appendNullTerminator(text.getBytes(StandardCharsets.UTF_16BE), ENCODING_UTF_16_BE); break;
            case ENCODING_LATIN_1:   bytes = appendNullTerminator(text.getBytes(StandardCharsets.ISO_8859_1), ENCODING_LATIN_1); break;
            default: throw new IllegalArgumentException("Invalid encoding: " + encoding);
        }
        return bytes;
    }

    public static byte[] prependByteOrderMark(String s) {

        byte[] withoutBOM = s.getBytes(StandardCharsets.UTF_16BE);
        byte[] withBOM    = new byte[withoutBOM.length + 2];

        System.arraycopy(BIG_BOM, 0, withBOM, 0, BIG_BOM.length);
        System.arraycopy(withoutBOM, 0, withBOM, 2, withoutBOM.length);
        return withBOM;
    }

    public static byte[] appendNullTerminator(byte[] bytes, byte encoding) {
        return Arrays.copyOf(
                bytes,
                bytes.length + (encoding == ENCODING_UTF_16 || encoding == ENCODING_UTF_16_BE ? 2 : 1)
        );
    }

    public static boolean hasByteOrderMark(byte[] data, int from) {
        byte[] bom = Arrays.copyOfRange(data, from, from + UTF_16_BOM_LENGTH);
        return Arrays.equals(bom, BIG_BOM) ||
                Arrays.equals(bom, LITTLE_BOM);
    }

    public static boolean isValidEncodingByte(byte encoding) {
        return  encoding == ENCODING_LATIN_1   ||
                encoding == ENCODING_UTF_16    ||
                encoding == ENCODING_UTF_16_BE ||
                encoding == ENCODING_UTF_8;
    }

    public static boolean isUTF16(Charset charset) {
        return charset.equals(StandardCharsets.UTF_16) ||
                charset.equals(StandardCharsets.UTF_16BE);
    }

    public static Charset getCharset(byte b) {
        Charset charset;
        switch (b) {
            case ENCODING_UTF_16:    charset = StandardCharsets.UTF_16;     break;
            case ENCODING_UTF_16_BE: charset = StandardCharsets.UTF_16BE;   break;
            case ENCODING_UTF_8:     charset = StandardCharsets.UTF_8;      break;
            case ENCODING_LATIN_1:   charset = StandardCharsets.ISO_8859_1; break;
            default: throw new IllegalArgumentException("Invalid encoding: " + b);
        }
        return charset;
    }
}
