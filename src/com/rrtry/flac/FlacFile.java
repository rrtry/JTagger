package com.rrtry.flac;

import com.rrtry.MediaFile;

import static com.rrtry.AbstractTagEditor.FLAC_MIME_TYPE;
import static com.rrtry.flac.AbstractMetadataBlock.BLOCK_TYPE_STREAMINFO;

public class FlacFile extends MediaFile<FlacTag> {

    @Override
    public FlacTagEditor getEditor(String mimeType) {
        if (!mimeType.equals(FLAC_MIME_TYPE)) {
            throw new IllegalArgumentException("Not a FLAC file");
        }
        return new FlacTagEditor();
    }

    public StreamInfoBlock getStreamInfo() {
        if (tag == null) throw new IllegalStateException("Error parsing metadata");
        return tag.getBlock(BLOCK_TYPE_STREAMINFO);
    }
}
