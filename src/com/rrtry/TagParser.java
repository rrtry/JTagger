package com.rrtry;

import java.io.RandomAccessFile;

public interface TagParser <T extends AbstractTag> {

    T parseTag(RandomAccessFile file);
}
