package com.jtagger;

import java.io.IOException;

public interface FileWrapper {

    long getFilePointer() throws IOException;
    long length() throws IOException;

    int read() throws IOException;
    int read(byte[] b) throws IOException;

    void write(byte[] b) throws IOException;
    void write(int b)    throws IOException;

    int  read(byte[] b, int off, int len)  throws IOException;
    void write(byte[] b, int off, int len) throws IOException;

    byte readByte() throws IOException;
    long readLong() throws IOException;

    int readInt() throws IOException;
    int readUnsignedByte() throws IOException;
    int skipBytes(int n) throws IOException;

    void seek(long pos) throws IOException;
    void setLength(long newLength) throws IOException;

    void close() throws IOException;
}
