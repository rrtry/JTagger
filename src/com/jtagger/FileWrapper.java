package com.jtagger;

import java.io.EOFException;
import java.io.IOException;

public interface FileWrapper {

    long getFilePointer() throws IOException;
    long length() throws IOException;

    int read() throws IOException;
    int read(byte[] b) throws IOException;

    void write(byte[] b) throws IOException;
    void write(int b)    throws IOException;
    void close()         throws IOException;

    int  read(byte[] b, int off, int len)  throws IOException;
    void write(byte[] b, int off, int len) throws IOException;

    void seek(long pos) throws IOException;
    void setLength(long newLength) throws IOException;

    default byte readByte() throws IOException {
        int b = read();
        if (b < 0) throw new EOFException();
        return (byte) b;
    }

    default int readInt() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        int ch3 = this.read();
        int ch4 = this.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    default long readLong() throws IOException {
        return ((long) (readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
    }

    default int readUnsignedByte() throws IOException {
        int ch = this.read();
        if (ch < 0) throw new EOFException();
        return ch;
    }

    default int skipBytes(int n) throws IOException {

        long pos;
        long len;
        long newpos;

        if (n <= 0) {
            return 0;
        }
        pos = getFilePointer();
        len = length();
        newpos = pos + n;
        if (newpos > len) {
            newpos = len;
        }
        seek(newpos);

        return (int) (newpos - pos);
    }
}
