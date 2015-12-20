package org.olympe.musicplayer.util;

import java.io.File;

import javafx.util.StringConverter;

public class FileNameStringConverter extends StringConverter<File>
{
    @Override
    public String toString(File object)
    {
        if (object == null)
            return null;
        return object.getName();
    }

    @Override
    public File fromString(String string)
    {
        throw new UnsupportedOperationException();
    }
}
