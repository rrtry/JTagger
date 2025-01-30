package com.jtagger;

import java.io.IOException;
import com.jtagger.FileWrapper;

public interface TagParser <T extends AbstractTag> {

    T parseTag(FileWrapper file) throws IOException;
}
