package org.olympe.musicplayer;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class Audio {

    private static ObservableMap<File, MediaPlayer> mediaPlayerCache = FXCollections.observableHashMap();

    private final ReadOnlyObjectWrapper<File> file;
    private ReadOnlyObjectWrapper<MediaPlayer> mediaPlayer;


    public Audio(File file) throws IOException {
        this.file = new ReadOnlyObjectWrapper<>(this, "file", file);
        loadAudioFile(file);
    }

    public static ObservableMap<File, MediaPlayer> getMediaPlayerCache() {
        return mediaPlayerCache;
    }

    //  --- Properties  ---

    //
    // File property
    //

    private void loadAudioFile(File file) throws IOException {
        MediaPlayer mediaPlayer = mediaPlayerCache.get(file);
        if (mediaPlayer == null) {
            Media media = new Media(file.toURI().toURL().toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayerCache.put(file, mediaPlayer);
        }
        setMediaPlayer(mediaPlayer);
    }

    public final ReadOnlyObjectProperty<File> fileReadOnlyProperty() {
        return file.getReadOnlyProperty();
    }

    //
    // Media player property.
    //

    public final File getFile() {
        return fileReadOnlyProperty().get();
    }

    private ReadOnlyObjectWrapper<MediaPlayer> mediaPlayerProperty() {
        if (mediaPlayer == null)
            mediaPlayer = new ReadOnlyObjectWrapper<>(this, "mediaPlayer");
        return mediaPlayer;
    }

    public final ReadOnlyObjectProperty<MediaPlayer> mediaPlayerReadOnlyProperty() {

        return mediaPlayerProperty().getReadOnlyProperty();
    }

    public final MediaPlayer getMediaPlayer() {
        return mediaPlayerReadOnlyProperty().get();
    }

    private void setMediaPlayer(MediaPlayer mediaPlayer) {
        mediaPlayerProperty().set(mediaPlayer);
    }
}
