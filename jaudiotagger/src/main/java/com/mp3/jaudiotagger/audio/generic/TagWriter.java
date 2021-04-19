package com.mp3.jaudiotagger.audio.generic;

import com.mp3.jaudiotagger.audio.AudioFile;
import com.mp3.jaudiotagger.audio.exceptions.CannotWriteException;
import com.mp3.jaudiotagger.tag.Tag;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Paul on 15/09/2015.
 */
public interface TagWriter
{
    public void delete(Tag tag, RandomAccessFile raf, RandomAccessFile tempRaf) throws IOException, CannotWriteException;


    /**
     * Write tag to file
     *
     * @param tag
     * @param raf
     * @param rafTemp
     * @throws com.mp3.jaudiotagger.audio.exceptions.CannotWriteException
     * @throws IOException
     */
    public void write(AudioFile af, Tag tag, RandomAccessFile raf, RandomAccessFile rafTemp) throws CannotWriteException, IOException;
}
