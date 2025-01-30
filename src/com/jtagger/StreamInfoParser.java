package com.jtagger;

import java.io.IOException;
import com.jtagger.FileWrapper;

public interface StreamInfoParser<I extends StreamInfo> {

    I parseStreamInfo(FileWrapper file) throws IOException;
}
