package com.rrtry;

import com.rrtry.id3.ID3V2TagEditor;
import com.rrtry.flac.FlacTagEditor;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static com.rrtry.AbstractTagEditor.FLAC_MIME_TYPE;
import static com.rrtry.AbstractTagEditor.MPEG_MIME_TYPE;

public class TagEditorFactory {

    public static AbstractTagEditor getEditor(String mimeType) {
        if (mimeType.equals(MPEG_MIME_TYPE)) return new ID3V2TagEditor();
        if (mimeType.equals(FLAC_MIME_TYPE)) return new FlacTagEditor();
        throw new NotImplementedException();
    }
}
