package org.olympe.musicplayer.impl.fxml;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.olympe.musicplayer.Audio;

import java.io.File;

import static javafx.scene.media.MediaPlayer.Status.PLAYING;

/**
 *
 */
public abstract class AbstractMusicPlayerFXMLController extends AudioListFXMLController {

    private IntegerProperty loadedIndex;
    private ObjectProperty<MediaPlayer> loadedMediaPlayer;
    private DoubleProperty currentProgress;
    private LongProperty currentDuration;
    private LongProperty totalDuration;

    public AbstractMusicPlayerFXMLController(Application application, Stage stage) {
        super(application, stage);
        loadedIndex = new SimpleIntegerProperty(this, "loadedIndex", -1);
        currentDuration = new SimpleLongProperty(this, "currentDuration", 0);
        totalDuration = new SimpleLongProperty(this, "totalDuration", 0);
        currentProgress = new SimpleDoubleProperty(this, "currentProgress", 0);
        loadedMediaPlayer = new SimpleObjectProperty<MediaPlayer>(this, "loadedMediaPlayer");
        loadedMediaPlayer.addListener((observable, oldValue, newValue) -> updateMediaPlayer(oldValue, newValue));
    }

    public final void stepForward() {
        stop();
        step(+1);
        setPlay(isPlaySelected());
    }

    public final void stepBackward() {
        stop();
        step(-1);
        setPlay(isPlaySelected());
    }

    public final void stop() {
        MediaPlayer mediaPlayer = getLoadedMediaPlayer();
        if (mediaPlayer != null)
            mediaPlayer.stop();
    }

    public final boolean isPlaying() {
        MediaPlayer mediaPlayer = getLoadedMediaPlayer();
        return mediaPlayer != null && mediaPlayer.getStatus() == PLAYING;
    }

    @Override
    protected void registerMediaPlayer(File file, MediaPlayer mediaPlayer) {
        super.registerMediaPlayer(file, mediaPlayer);
        int repeat = computeRepeat();
        mediaPlayer.setCycleCount(repeat == 1 ? Integer.MAX_VALUE : 1);
        mediaPlayer.muteProperty().bind(muteProperty());
        mediaPlayer.volumeProperty().bind(volumeProperty());
        mediaPlayer.setOnEndOfMedia(this::onEndOfMedia);
        mediaPlayer.setOnError(this::onErrorMedia);
        mediaPlayer.setOnHalted(this::onHalted);
        mediaPlayer.setOnPaused(this::onPaused);
        mediaPlayer.setOnPlaying(this::onPlaying);
        mediaPlayer.setOnReady(this::onReady);
        mediaPlayer.setOnRepeat(this::onRepeat);
        mediaPlayer.setOnStalled(this::onStalled);
        mediaPlayer.setOnStopped(this::onStopped);
    }

    @Override
    protected void unregisterMediaPlayer(File file, MediaPlayer mediaPlayer) {
        super.unregisterMediaPlayer(file, mediaPlayer);
        mediaPlayer.muteProperty().unbind();
        mediaPlayer.setOnEndOfMedia(null);
        mediaPlayer.setOnError(null);
        mediaPlayer.setOnHalted(null);
        mediaPlayer.setOnPaused(null);
        mediaPlayer.setOnPlaying(null);
        mediaPlayer.setOnReady(null);
        mediaPlayer.setOnRepeat(null);
        mediaPlayer.setOnStalled(null);
        mediaPlayer.setOnStopped(null);
        mediaPlayer.dispose();
    }

    public final void toggleRepeat() {
        int repeat = computeRepeat();
        Audio.getMediaPlayerCache().values().stream().forEach(mediaPlayer -> mediaPlayer.setCycleCount(repeat == 1 ? Integer.MAX_VALUE : 1));
    }

    public final void toggleMute() {
        // Do nothing: the toggle button selected
        // property should be binded to all media
        // players mute property.
    }

    public final void togglePlay() {
        setPlay(!isPlaying());
    }

    public final void setPlay(boolean play) {
        MediaPlayer mediaPlayer = getLoadedMediaPlayer();
        if (play)
            mediaPlayer.play();
        else
            mediaPlayer.pause();
        setPlaySelected(play);
    }

    public final void step(int offset) {
        int index = compute(offset);
        loadedIndex.set(index);
        Audio audio = getData().get(index);
        loadedMediaPlayer.set(audio.getMediaPlayer());
    }

    public final boolean step(Audio audio) {
        int index = getData().indexOf(audio);
        int offset = getLoadedIndex() - index;
        offset = offset % getData().size();
        step(offset);
        return true;
    }

    public final int getLoadedIndex() {
        return loadedIndex.get();
    }

    public final IntegerProperty loadedIndexProperty() {
        return loadedIndex;
    }

    public final MediaPlayer getLoadedMediaPlayer() {
        return loadedMediaPlayer.get();
    }

    public final DoubleProperty currentProgressProperty() {
        return currentProgress;
    }

    public final LongProperty currentDurationProperty() {
        return currentDuration;
    }

    public final LongProperty totalDurationProperty() {
        return totalDuration;
    }

    public final void seek(double value) {
        MediaPlayer player = getLoadedMediaPlayer();
        if (player != null) {
            Platform.runLater(() -> {
                // can only seek when the player is ready.
                player.seek(Duration.millis((value / 100) * totalDuration.get()));
                if (player.getStatus() != MediaPlayer.Status.PLAYING)
                    currentDuration.set((long) ((value / 100) * totalDuration.get()));
            });
        }
    }

    protected abstract void updateMediaPlayer(MediaPlayer mediaPlayer, MediaPlayer newValue);

    protected abstract int computeRepeat();

    protected abstract DoubleProperty volumeProperty();

    protected abstract BooleanProperty muteProperty();

    protected abstract boolean isPlaySelected();

    protected abstract void setPlaySelected(boolean b);

    protected abstract int compute(int offset);

    protected abstract void onStopped();

    protected abstract void onStalled();

    protected abstract void onRepeat();

    protected abstract void onReady();

    protected abstract void onPlaying();

    protected abstract void onPaused();

    protected abstract void onHalted();

    protected abstract void onErrorMedia();

    protected abstract void onEndOfMedia();
}
