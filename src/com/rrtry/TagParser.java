package com.rrtry;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface TagParser <T extends ID3Tag> {

    T parse(RandomAccessFile file) throws IOException;
}
