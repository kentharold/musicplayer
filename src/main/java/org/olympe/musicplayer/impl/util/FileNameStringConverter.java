package org.olympe.musicplayer.impl.util;

import javafx.util.StringConverter;

import java.io.File;

public class FileNameStringConverter extends StringConverter<File> {

    @Override
    public String toString(File object) {
        if (object == null)
            return null;
        return object.getName();
    }

    @Override
    public File fromString(String string) {
        throw new UnsupportedOperationException();
    }
}
