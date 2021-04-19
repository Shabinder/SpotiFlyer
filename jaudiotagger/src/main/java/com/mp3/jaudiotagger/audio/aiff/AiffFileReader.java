package com.mp3.jaudiotagger.audio.aiff;

import com.mp3.jaudiotagger.audio.exceptions.CannotReadException;
import com.mp3.jaudiotagger.audio.generic.AudioFileReader2;
import com.mp3.jaudiotagger.audio.generic.GenericAudioHeader;
import com.mp3.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.IOException;

/**
 * Reads Audio and Metadata information contained in Aiff file.
 */
public class AiffFileReader extends AudioFileReader2 {
    private AiffInfoReader ir = new AiffInfoReader();
    private AiffTagReader im = new AiffTagReader();

    @Override
    protected GenericAudioHeader getEncodingInfo(File file) throws CannotReadException, IOException {
        return ir.read(file);
    }

    @Override
    protected Tag getTag(File file) throws CannotReadException, IOException {
        return im.read(file);
    }
}
