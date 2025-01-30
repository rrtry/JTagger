package com.jtagger;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface StreamInfoParser<I extends StreamInfo> {

    I parseStreamInfo(RandomAccessFile file) throws IOException;
}
