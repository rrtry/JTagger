package com.rrtry;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface TagParser <T extends Tag> {

    T parse(RandomAccessFile file) throws IOException;
}
