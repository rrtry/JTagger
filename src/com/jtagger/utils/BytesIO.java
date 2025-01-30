package com.jtagger.utils;

import com.jtagger.FileWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BytesIO {

    public static final int PADDING_MAX = 1024 * 1024;
    public static final int PADDING_MIN = 1024;
    public static final int BUFFER_SIZE = 8192;

    public static int getPadding(int fileLength) {
        return Math.min(Math.max(fileLength / 100, PADDING_MIN), PADDING_MAX);
    }

    public static void copyBytes(InputStream in, OutputStream out) throws IOException {

        int size;
        int left = in.available();

        byte[] buffer = new byte[BUFFER_SIZE];
        while (left > 0) {
            size = Math.min(buffer.length, left);
            in.read(buffer, 0, size);
            out.write(buffer, 0, size);
            left -= size;
        }
    }

    public static void writeBlock(
            FileWrapper file,
            byte[] buffer,
            int offset) throws IOException
    {
        file.seek(offset);
        file.write(buffer);
    }

    public static void moveBlock(
            FileWrapper file,
            int from,
            int to,
            int sizeDiff,
            int count) throws IOException
    {
        if (sizeDiff == 0) {
            System.out.println("FileIO.moveBlock: sizeDiff == 0");
            return;
        }
        if (from == to) {
            throw new IllegalArgumentException("Destination position cannot be equal to source position");
        }

        final int fLen = (int) file.length();
        byte[] buffer  = new byte[BUFFER_SIZE];

        int size;
        int left = count;

        if (to > from) {
            while (left > 0) {
                size = Math.min(BUFFER_SIZE, left);
                file.seek(from + left - size);
                file.read(buffer, 0, size);
                file.seek(left + to - size);
                file.write(buffer, 0, size);
                left -= size;
            }
        }
        else {
            int copied = 0;
            while (left > 0) {
                size = Math.min(BUFFER_SIZE, left);
                file.seek(from + copied);
                file.read(buffer, 0, size);
                file.seek(to + copied);
                file.write(buffer, 0, size);
                copied += size;
                left -= size;
            }
        }
        file.setLength(fLen + sizeDiff);
    }
}
