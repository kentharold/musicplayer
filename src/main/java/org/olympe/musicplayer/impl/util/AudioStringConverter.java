package org.olympe.musicplayer.impl.util;

import javafx.util.StringConverter;
import org.olympe.musicplayer.Audio;

/**
 *
 */
public class AudioStringConverter extends StringConverter<Audio> {

    @Override
    public String toString(Audio object) {
        if (object == null)
            return null;
        return object.toString();
    }

    @Override
    public Audio fromString(String string) {
        return null;
    }
}
