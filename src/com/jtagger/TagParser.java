package com.jtagger;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface TagParser <T extends AbstractTag> {

    T parseTag(RandomAccessFile file) throws IOException;
}
