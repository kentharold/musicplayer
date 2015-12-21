package org.olympe.musicplayer.fxml;

import java.io.File;
import java.util.Collection;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import static javafx.application.Platform.runLater;
import static javafx.scene.media.MediaPlayer.Status.PLAYING;

import org.olympe.musicplayer.bean.model.Audio;
import static org.olympe.musicplayer.bean.model.Audio.getMediaPlayerCache;

/**
 *
 */
public abstract class AbstractMusicPlayerFXMLController extends AudioListFXMLController
{
    private IntegerProperty loadedIndex;
    private ObjectProperty<Audio> loadedAudio;
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
        loadedAudio = new SimpleObjectProperty<>(this, "loadedAudio");
        loadedAudio.addListener(this::updateAudio);
    }

    public final void stepForward()
    {
        logger.entering("AbstractMusicPlayerFXMLController", "stepForward");
        stop();
        step(+1);
        setPlay(isPlaySelected());
        logger.exiting("AbstractMusicPlayerFXMLController", "stepForward");
    }

    public final void stepBackward()
    {
        logger.entering("AbstractMusicPlayerFXMLController", "stepBackward");
        stop();
        step(-1);
        setPlay(isPlaySelected());
        logger.entering("AbstractMusicPlayerFXMLController", "stepBackward");
    }

    public final void stop()
    {
        logger.entering("AbstractMusicPlayerFXMLController", "stop");
        MediaPlayer mediaPlayer = getLoadedMediaPlayer();
        if (mediaPlayer != null)
            mediaPlayer.stop();
        logger.exiting("AbstractMusicPlayerFXMLController", "stop");
    }

    public final boolean isPlaying()
    {
        logger.entering("AbstractMusicPlayerFXMLController", "isPlaying");
        MediaPlayer mediaPlayer = getLoadedMediaPlayer();
        MediaPlayer.Status status = null;
        if (mediaPlayer != null)
            status = mediaPlayer.getStatus();
        boolean result = status != null && status == PLAYING;
        logger.exiting("AbstractMusicPlayerFXMLController", "isPlaying", result);
        return result;
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

    public final void togglePlay()
    {
        logger.entering("AbstractMusicPlayerFXMLController", "togglePlay");
        setPlay(!isPlaying());
        logger.exiting("AbstractMusicPlayerFXMLController", "togglePlay");
    }

    public final void setPlay(boolean play)
    {
        logger.entering("AbstractMusicPlayerFXMLController", "setPlay", play);
        MediaPlayer mediaPlayer = getLoadedMediaPlayer();
        if (play)
            mediaPlayer.play();
        else
            mediaPlayer.pause();
        setPlaySelected(play);
        logger.exiting("AbstractMusicPlayerFXMLController", "setPlay");
    }

    public final void step(int offset)
    {
        logger.entering("AbstractMusicPlayerFXMLController", "step", offset);
        int index = compute(offset);
        loadedIndex.set(index);
        Audio audio = getData().get(index);
        loadedAudio.set(audio);
        logger.exiting("AbstractMusicPlayerFXMLController", "step");
    }

    public final void step(Audio audio)
    {
        logger.entering("AbstractMusicPlayerFXMLController", "step", audio);
        if (audio != null)
        {
            loadedAudio.set(audio);
            int index = getData().indexOf(audio);
            loadedIndex.set(index);
        }
        logger.exiting("AbstractMusicPlayerFXMLController", "step");
    }

    public final int getLoadedIndex()
    {
        return loadedIndex.get();
    }

    public final IntegerProperty loadedIndexProperty()
    {
        return loadedIndex;
    }

    public final MediaPlayer getLoadedMediaPlayer()
    {
        Audio audio = loadedAudio.get();
        MediaPlayer player = null;
        if (audio != null)
            player = audio.getMediaPlayer();
        return player;
    }

    public final Audio getLoadedAudio()
    {
        return loadedAudio.get();
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

    protected abstract void updateAudio(ObservableValue<? extends Audio> observable, Audio oldValue, Audio newValue);

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
