package com.rrtry;

import java.io.RandomAccessFile;

public interface StreamInfoParser<I extends StreamInfo> {

    I parseStreamInfo(RandomAccessFile file);
}
