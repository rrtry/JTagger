package com.jtagger.mp3.id3;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.jtagger.mp3.id3.ID3V2Tag.ID3V2_3;
import static com.jtagger.mp3.id3.ID3V2Tag.ID3V2_4;

public class TextEncoding {

    public static byte[] BOM_BE = new byte[] { (byte) 0xFE, (byte) 0xFF };
    public static byte[] BOM_LE = new byte[] { (byte) 0xFF, (byte) 0xFE };

    public static final byte ENCODING_LATIN_1   = 0x00;
    public static final byte ENCODING_UTF_16    = 0x01;
    public static final byte ENCODING_UTF_16_BE = 0x02;
    public static final byte ENCODING_UTF_8     = 0x03;

    public static boolean isNumeric(String s) {
        if (s.isBlank()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static String getString(byte[] buffer, int from, int length, byte encoding) {
        return new String(
                Arrays.copyOfRange(
                        buffer,
                        from,
                        from + length),
                TextEncoding.getCharset(encoding)
        ).replace("\0", "");
    }

    public static int getStringLength(byte[] bytes, int offset, byte encoding) {

        boolean isUTF16 = encoding == ENCODING_UTF_16 || encoding == ENCODING_UTF_16_BE;
        boolean hasBOM  = hasByteOrderMark(bytes, offset);

        int chLen  = isUTF16 ? 2 : 1;
        int length = hasBOM ? 2 : 0;

        int index = offset + length;
        while (index < bytes.length) {

            if (isUTF16) {
                if ((index + 1) < bytes.length) {
                    if (bytes[index] == 0x0 && bytes[index + 1] == 0x0) {
                        length += 2;
                        break;
                    }
                } else {
                    length++;
                    break;
                }
            } else if (bytes[index] == 0x0) {
                length++;
                break;
            }
            length += chLen;
            index += chLen;
        }
        return length;
    }

    public static byte getEncodingForVersion(byte version) {
        if (version == ID3V2_3) return ENCODING_UTF_16;
        if (version == ID3V2_4) return ENCODING_UTF_8;
        throw new IllegalArgumentException("Invalid version: " + version);
    }

    public static byte[] getStringBytes(String text, byte encoding) {
        return appendNull(text.getBytes(getCharset(encoding)), encoding);
    }

    public static byte[] appendNull(byte[] bytes, byte encoding) {
        return Arrays.copyOf(
                bytes,
                bytes.length + (encoding == ENCODING_UTF_16 || encoding == ENCODING_UTF_16_BE ? 2 : 1)
        );
    }

    public static boolean hasByteOrderMark(byte[] data, int from) {
        byte[] bom = Arrays.copyOfRange(data, from, from + 2);
        return Arrays.equals(bom, BOM_BE) ||
                Arrays.equals(bom, BOM_LE);
    }

    public static boolean isValidEncodingByte(byte encoding) {
        return  encoding == ENCODING_LATIN_1   ||
                encoding == ENCODING_UTF_16    ||
                encoding == ENCODING_UTF_16_BE ||
                encoding == ENCODING_UTF_8;
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
