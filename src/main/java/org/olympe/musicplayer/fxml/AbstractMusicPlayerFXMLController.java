package org.olympe.musicplayer.fxml;

import java.io.File;
import java.util.Collection;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import static javafx.application.Platform.runLater;

import org.olympe.musicplayer.bean.model.Audio;
import static org.olympe.musicplayer.bean.model.Audio.getMediaPlayerCache;

/**
 *
 */
public abstract class AbstractMusicPlayerFXMLController extends AudioListFXMLController
{
    private IntegerProperty loadedIndex;
    private DoubleProperty currentProgress;
    private LongProperty currentDuration;
    private LongProperty totalDuration;

    public AbstractMusicPlayerFXMLController(Application application, Stage stage)
    {
        super(application, stage);
        loadedIndex = new SimpleIntegerProperty(this, "loadedIndex", -1);
        currentDuration = new SimpleLongProperty(this, "currentDuration", 0);
        totalDuration = new SimpleLongProperty(this, "totalDuration", 0);
        currentProgress = new SimpleDoubleProperty(this, "currentProgress", 0);
    }

    public final void stepForward()
    {
        logger.entering("AbstractMusicPlayerFXMLController", "stepForward");
        stop();
        load(+1);
        setPlay(isPlaySelected());
        logger.exiting("AbstractMusicPlayerFXMLController", "stepForward");
    }

    public final void stepBackward()
    {
        logger.entering("AbstractMusicPlayerFXMLController", "stepBackward");
        stop();
        load(-1);
        setPlay(isPlaySelected());
        logger.entering("AbstractMusicPlayerFXMLController", "stepBackward");
    }

    public final void toggleRepeat()
    {
        logger.entering("AbstractMusicPlayerFXMLController", "toggleRepeat");
        Collection<MediaPlayer> col = getMediaPlayerCache().values();
        Stream<MediaPlayer> stream = col.stream();
        stream.forEach(this::updateCycleCount);
        logger.exiting("AbstractMusicPlayerFXMLController", "toggleRepeat");
    }

    public final void toggleMute()
    {
        // Do nothing: the toggle button selected
        // property should be bound to all media
        // players mute property.
    }

    public final void load(int offset)
    {
        logger.entering("AbstractMusicPlayerFXMLController", "load", offset);
        int index = compute(offset);
        loadedIndex.set(index);
        Audio audio = getData().get(index);
        loadedAudioProperty().set(audio);
        logger.exiting("AbstractMusicPlayerFXMLController", "load");
    }

    @Override
    public final void load(Audio audio)
    {
        logger.entering("AbstractMusicPlayerFXMLController", "load", audio);
        if (audio != null)
        {
            loadedAudioProperty().set(audio);
            int index = getData().indexOf(audio);
            loadedIndex.set(index);
        }
        logger.exiting("AbstractMusicPlayerFXMLController", "load");
    }

    public final int getLoadedIndex()
    {
        return loadedIndex.get();
    }

    public final IntegerProperty loadedIndexProperty()
    {
        return loadedIndex;
    }

    public final DoubleProperty currentProgressProperty()
    {
        return currentProgress;
    }

    public final LongProperty currentDurationProperty()
    {
        return currentDuration;
    }

    public final LongProperty totalDurationProperty()
    {
        return totalDuration;
    }

    public final void seek(double value)
    {
        logger.entering("AbstractMusicPlayerFXMLController", "seek", value);
        MediaPlayer player = getLoadedMediaPlayer();
        if (player != null)
        {
            // can only seek when the player is ready.
            runLater(() -> _seek(player, value));
        }
        logger.exiting("AbstractMusicPlayerFXMLController", "seek");
    }

    @Override
    protected void registerMediaPlayer(File file, MediaPlayer mediaPlayer)
    {
        logger.entering("AbstractMusicPlayerFXMLController", "registerMediaPlayer", new Object[]{file, mediaPlayer});
        updateCycleCount(mediaPlayer);
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
        logger.exiting("AbstractMusicPlayerFXMLController", "registerMediaPlayer");
    }

    @Override
    protected void unregisterMediaPlayer(File file, MediaPlayer mediaPlayer)
    {
        logger.entering("AbstractMusicPlayerFXMLController", "unregisterMediaPlayer", new Object[]{file, mediaPlayer});
        updateCycleCount(mediaPlayer); // not very useful.
        mediaPlayer.muteProperty().unbind();
        mediaPlayer.volumeProperty().unbind();
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
        logger.exiting("AbstractMusicPlayerFXMLController", "registerMediaPlayer");
    }

    protected abstract int computeRepeat();

    protected abstract DoubleProperty volumeProperty();

    protected abstract BooleanProperty muteProperty();

    protected abstract boolean isPlaySelected();

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

    private void _seek(MediaPlayer player, double value)
    {
        logger.entering("AbstractMusicPlayerFXMLController", "_seek", new Object[]{player, value});
        player.seek(Duration.millis((value / 100) * totalDuration.get()));
        if (player.getStatus() != MediaPlayer.Status.PLAYING)
            currentDuration.set((long) ((value / 100) * totalDuration.get()));
        logger.exiting("AbstractMusicPlayerFXMLController", "_seek");
    }

    private void updateCycleCount(MediaPlayer mediaPlayer)
    {
        logger.entering("AbstractMusicPlayerFXMLController", "updateCycleCount", mediaPlayer);
        int repeat = computeRepeat();
        mediaPlayer.setCycleCount(repeat == 1 ? Integer.MAX_VALUE : 1);
        logger.exiting("AbstractMusicPlayerFXMLController", "updateCycleCount");
    }
}
