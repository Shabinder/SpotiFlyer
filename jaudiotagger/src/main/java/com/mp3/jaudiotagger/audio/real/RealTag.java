package com.mp3.jaudiotagger.audio.real;

import com.mp3.jaudiotagger.audio.generic.GenericTag;
import com.mp3.jaudiotagger.tag.FieldDataInvalidException;
import com.mp3.jaudiotagger.tag.FieldKey;
import com.mp3.jaudiotagger.tag.KeyNotFoundException;
import com.mp3.jaudiotagger.tag.TagField;

public class RealTag extends GenericTag
{
    public String toString()
    {
        String output = "REAL " + super.toString();
        return output;
    }

    public TagField createCompilationField(boolean value) throws KeyNotFoundException, FieldDataInvalidException
    {
        return createField(FieldKey.IS_COMPILATION,String.valueOf(value));
    }
}
