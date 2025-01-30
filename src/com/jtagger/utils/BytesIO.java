package com.jtagger.utils;

import java.io.*;
import java.util.UUID;

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
            RandomAccessFile file,
            byte[] buffer,
            int offset) throws IOException
    {
        file.seek(offset);
        file.write(buffer);
    }

    public static void copyBlock(
            byte[] tempBuffer,
            RandomAccessFile from,
            RandomAccessFile to,
            int count) throws IOException
    {
        int copied = 0;
        int bufferSize;

        while (copied < count) {
            bufferSize = Math.min(tempBuffer.length, count - copied);
            from.read(tempBuffer, 0, bufferSize);
            to.write(tempBuffer, 0, bufferSize);
            copied += bufferSize;
        }
    }

    public static void overwrite(
            RandomAccessFile file,
            byte[] tagBuffer,
            byte[] padding,
            int oldTagEndPos,
            int newTagStartPos,
            int sizeDiff,
            int count) throws IOException
    {
        File temp;
        RandomAccessFile tempFile;

        temp     = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
        tempFile = new RandomAccessFile(temp.getAbsolutePath(), "rw");

        byte[] tempBuffer = new byte[BUFFER_SIZE];
        long fLength = file.length();
        int copyLen  = count;

        file.seek(oldTagEndPos);
        copyBlock(tempBuffer, file, tempFile, copyLen);
        copyLen = (int) tempFile.length();

        tempFile.seek(0);
        file.seek(newTagStartPos);
        file.write(tagBuffer);

        if (padding != null) {
            file.write(padding);
        }

        copyBlock(tempBuffer, tempFile, file, copyLen);
        file.setLength(fLength + sizeDiff);
        tempFile.close();

        if (!temp.delete()) {
            System.err.println("Main.overwrite: failed to delete temp file");
        }
    }

    public static void moveBlock(
            RandomAccessFile file,
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
