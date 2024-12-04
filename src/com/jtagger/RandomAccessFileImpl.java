package com.jtagger;

import java.io.IOException;
import java.io.RandomAccessFile;

class RandomAccessFileImpl implements FileWrapper {

    private RandomAccessFile file;

    public RandomAccessFileImpl(RandomAccessFile file) {
        this.file = file;
    }

    @Override
    public long getFilePointer() throws IOException {
        return file.getFilePointer();
    }

    @Override
    public long length() throws IOException {
        return file.length();
    }

    @Override
    public int read() throws IOException {
        return file.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return file.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return file.read(b, off, len);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        file.write(b, off, len);
    }

    @Override
    public byte readByte() throws IOException {
        return file.readByte();
    }

    @Override
    public long readLong() throws IOException {
        return file.readLong();
    }

    @Override
    public int readInt() throws IOException {
        return file.readInt();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return file.readUnsignedByte();
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return file.skipBytes(n);
    }

    @Override
    public void seek(long pos) throws IOException {
        file.seek(pos);
    }

    @Override
    public void setLength(long newLength) throws IOException {
        file.setLength(newLength);
    }

    @Override
    public void write(byte[] b) throws IOException {
        file.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        file.write(b);
    }

    @Override
    public void close() throws IOException {
        file.close();
        file = null;
    }
}
