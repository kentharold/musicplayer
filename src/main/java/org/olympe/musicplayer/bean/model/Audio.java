package org.olympe.musicplayer.bean.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

/**
 *
 */
public class Audio
{
    private static ObservableMap<File, MediaPlayer> mediaPlayerCache;
    private static ObservableMap<File, Tag> tagCache;

    static
    {
        mediaPlayerCache = FXCollections.observableHashMap();
        tagCache = FXCollections.observableHashMap();
    }

    private final ReadOnlyObjectWrapper<File> file;
    private ReadOnlyObjectWrapper<MediaPlayer> mediaPlayer;
    private ReadOnlyObjectWrapper<Tag> tag;

    public Audio(File file) throws IOException
    {
        this.file = new ReadOnlyObjectWrapper<>(this, "file", file);
        loadAudioFile(file);
    }

    public static ObservableMap<File, MediaPlayer> getMediaPlayerCache()
    {
        return mediaPlayerCache;
    }

    public final ReadOnlyObjectProperty<File> fileReadOnlyProperty()
    {
        return file.getReadOnlyProperty();
    }

    public final File getFile()
    {
        return fileReadOnlyProperty().get();
    }

    public final ReadOnlyObjectProperty<MediaPlayer> mediaPlayerReadOnlyProperty()
    {
        return mediaPlayerProperty().getReadOnlyProperty();
    }

    public final MediaPlayer getMediaPlayer()
    {
        return mediaPlayerReadOnlyProperty().get();
    }

    private void setMediaPlayer(MediaPlayer mediaPlayer)
    {
        mediaPlayerProperty().set(mediaPlayer);
    }

    public ReadOnlyObjectProperty<Tag> tagReadOnlyProperty()
    {
        return tagProperty().getReadOnlyProperty();
    }

    public Tag getTag()
    {
        return tagProperty().get();
    }

    private void setTag(Tag tag)
    {
        tagProperty().set(tag);
    }

    private void loadAudioFile(File file) throws IOException
    {
        MediaPlayer mediaPlayer = mediaPlayerCache.get(file);
        if (mediaPlayer == null)
        {
            URL url = file.toURI().toURL();
            Media media = new Media(url.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayerCache.put(file, mediaPlayer);
        }
        setMediaPlayer(mediaPlayer);
        //
        Tag tag = tagCache.get(file);
        if (tag == null)
        {
            try
            {
                AudioFile audioFile = AudioFileIO.read(file);
                tag = audioFile.getTag();
            }
            catch (CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException e)
            {
                throw new IOException(e);
            }
            if (tag == null)
                throw new IOException("can not read audio tag.");
        }
        setTag(tag);
    }

    private ReadOnlyObjectWrapper<MediaPlayer> mediaPlayerProperty()
    {
        if (mediaPlayer == null)
            mediaPlayer = new ReadOnlyObjectWrapper<>(this, "mediaPlayer");
        return mediaPlayer;
    }

    private ReadOnlyObjectWrapper<Tag> tagProperty()
    {
        if (tag == null)
            tag = new ReadOnlyObjectWrapper<>(this, "tag");
        return tag;
    }
}
