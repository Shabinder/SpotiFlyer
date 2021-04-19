/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Rapha�l Slinckx <raphael@slinckx.net>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.mp3.jaudiotagger.audio.wav;

import com.mp3.jaudiotagger.audio.exceptions.CannotReadException;
import com.mp3.jaudiotagger.audio.generic.AudioFileReader2;
import com.mp3.jaudiotagger.audio.generic.GenericAudioHeader;
import com.mp3.jaudiotagger.tag.Tag;
import com.mp3.jaudiotagger.tag.TagOptionSingleton;
import com.mp3.jaudiotagger.tag.wav.WavTag;

import java.io.File;
import java.io.IOException;

/**
 * Reads Audio and Metadata information contained in Wav file.
 */
public class WavFileReader extends AudioFileReader2
{
    public WavFileReader()
    {

    }

    protected GenericAudioHeader getEncodingInfo(File file) throws CannotReadException, IOException
    {
        return new WavInfoReader(file.getPath()).read(file);
    }

    @Override
    protected Tag getTag(File file) throws IOException, CannotReadException
    {           
        WavTag tag =  new WavTagReader(file.getPath()).read(file);
        switch (TagOptionSingleton.getInstance().getWavOptions())
        {
            case READ_ID3_ONLY_AND_SYNC:
            case READ_ID3_UNLESS_ONLY_INFO_AND_SYNC:
            case READ_INFO_ONLY_AND_SYNC:
            case READ_INFO_UNLESS_ONLY_ID3_AND_SYNC:
                tag.syncTagsAfterRead();
        }
        return tag;
    }
}