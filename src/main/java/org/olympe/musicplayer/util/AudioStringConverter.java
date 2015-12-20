package org.olympe.musicplayer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.util.StringConverter;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import org.olympe.musicplayer.bean.model.Audio;

/**
 *
 */
public class AudioStringConverter extends StringConverter<Audio>
{
    private static final String SEPARATOR;

    static
    {
        SEPARATOR = " - ";
    }

    private List<Flag> flags;

    public AudioStringConverter(Flag... flags)
    {
        super();
        this.flags = new ArrayList<>();
        if (flags == null || flags.length == 0)
            this.flags.add(Flag.FILE_NAME);
        else
            this.flags.addAll(Arrays.asList(flags));
    }

    @Override
    public String toString(Audio audio)
    {
        if (audio == null)
            return null;
        StringBuilder sb = new StringBuilder();
        flags.stream().forEach(flag -> append(sb, audio, flag));
        return sb.toString();
    }

    @Override
    public Audio fromString(String string)
    {
        return null;
    }

    private void append(StringBuilder sb, Audio audio, Flag flag)
    {
        File file = audio.getFile();
        Tag tag = audio.getTag();
        switch (flag)
        {
            case RESET:
                // ignore this flag
                // sb.replace(0, sb.length(), "");
                break;
            case FILE_NAME:
                sb.append(file.getName());
                break;
            case FILE_PATH:
                sb.append(file.getAbsolutePath());
                break;
            case SEPARATOR_FLAG:
                sb.append(SEPARATOR);
                break;
            case TRACK_ARTIST:
                String artist = tag.getFirst(FieldKey.ALBUM_ARTIST);
                if (artist == null || artist.isEmpty())
                    artist = tag.getFirst(FieldKey.ARTIST);
                if (artist == null || artist.isEmpty())
                    artist = "Unknown";
                sb.append(artist);
                break;
            case TRACK_TITLE:
                String title = tag.getFirst(FieldKey.TITLE);
                if (title == null || title.isEmpty())
                    title = "Unknown";
                sb.append(title);
                break;
            case TRACK_ALBUM:
                String album = tag.getFirst(FieldKey.ALBUM);
                if (album == null || album.isEmpty())
                    album = "Unknown";
                sb.append(album);
                break;
        }
    }

    public enum Flag
    {
        FILE_PATH, FILE_NAME, TRACK_ARTIST, TRACK_TITLE, TRACK_ALBUM, SEPARATOR_FLAG, RESET
    }
}
