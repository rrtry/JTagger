package com.jtagger.ogg;

import java.util.Arrays;
import java.util.Objects;

public class OggPacket {

    private static final int DEFAULT_CAPACITY = 255;
    private static final int MAX_ARRAY_SIZE   = Integer.MAX_VALUE - 8;

    private int count  = 0;
    private byte[] buf = new byte[DEFAULT_CAPACITY];

    public OggPacket() {

    }

    public OggPacket(byte[] packetData) {
        this.count = packetData.length;
        this.buf   = packetData;
    }

    // from ByteArrayOutputStream.java
    private void ensureCapacity(int minCapacity) {
        if (minCapacity - buf.length > 0)
            grow(minCapacity);
    }

    private void grow(int minCapacity) {
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        buf = Arrays.copyOf(buf, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0)
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    public void write(byte[] b, int off, int len) {
        Objects.checkFromIndexSize(off, len, b.length);
        ensureCapacity(count + len);
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    public byte[] getBuffer() {
        return buf;
    }

    public byte[] getData() {
        return Arrays.copyOf(buf, count);
    }
}
