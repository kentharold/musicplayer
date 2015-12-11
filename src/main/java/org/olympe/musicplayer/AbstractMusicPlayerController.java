package org.olympe.musicplayer;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;
import org.olympe.musicplayer.util.ColorThief;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractMusicPlayerController implements MusicPlayerController {

    private ListProperty<File> musicFiles = new SimpleListProperty<>();
    private MapProperty<File, MediaPlayer> mediaPlayers = new SimpleMapProperty<>();
    private IntegerProperty currentIndex = new SimpleIntegerProperty(-1);
    private LongProperty totalTime = new SimpleLongProperty();
    private BooleanProperty currentDurationChangingInternally = new SimpleBooleanProperty(false);
    private BooleanProperty isPlaying = new SimpleBooleanProperty(false);
    private ChangeListener<Duration> currentTimeChangeListener = new ChangeListener<Duration>() {
        @Override
        public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
            if (newValue != null) {
                long currentMillis = (long) newValue.toMillis();
                currentTime.set(currentMillis);
                currentDurationChangingInternally.set(true);
                double duration = (newValue.toMillis() / totalTime.get()) * 100;
                currentDuration.set(duration);
                currentDurationChangingInternally.set(false);
            }
        }
    };
    private ChangeListener<Duration> totalTimeChangeListener = (observable, oldValue, newValue) -> {
        if (newValue != null) {
            totalTime.set((long) newValue.toMillis());
        }
    };
    private ObjectProperty<Image> coverImage = new SimpleObjectProperty<>();
    private Map<Object, Image> coversCache = new HashMap<>();
    private Map<MediaPlayer, Image> coversMap = new HashMap<>();

    private ObjectProperty<MediaPlayer> currentMediaPlayer = new SimpleObjectProperty<>();
    private IntegerProperty repeat = new SimpleIntegerProperty(0);
    private DoubleProperty currentDuration = new SimpleDoubleProperty(0.0) {
        @Override
        protected void invalidated() {
            if (!currentDurationChangingInternally.get()) {
                seek(get());
            }
        }
    };
    private LongProperty currentTime = new SimpleLongProperty();
    private DoubleProperty volume = new SimpleDoubleProperty(1.0) {
        @Override
        protected void invalidated() {
            mediaPlayers.values().forEach(mediaPlayer -> mediaPlayer.setVolume(get()));
        }
    };
    private BooleanProperty mute = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            mediaPlayers.values().forEach(mediaPlayer -> mediaPlayer.setMute(get()));
        }
    };
    private ChangeListener<MediaPlayer> mediaPlayerChangeListener = (observable, oldValue, newValue) -> {
        if (oldValue != null) {
            oldValue.currentTimeProperty().removeListener(currentTimeChangeListener);
            oldValue.totalDurationProperty().removeListener(totalTimeChangeListener);
            oldValue.stop();
        }
        currentDuration.set(0.0);
        currentTime.set(0);
        if (newValue != null) {
            newValue.currentTimeProperty().addListener(currentTimeChangeListener);
            newValue.totalDurationProperty().addListener(totalTimeChangeListener);

            coverImage.set(coversMap.get(newValue));
            if (newValue.getTotalDuration() != null) {
                totalTime.set((long) newValue.getTotalDuration().toMillis());
            }
            if (isPlaying.get())
                newValue.play();
        }
    };
    private static final String EMPTY_COVER_IMAGE_URL = "https://image.freepik.com/free-icon/music-cd_318-48567.png";
    private final Image emptyCoverImage = new Image(EMPTY_COVER_IMAGE_URL);
    private Map<Image, Color> predominantColorsCache = new HashMap<>();

    public AbstractMusicPlayerController() {
        musicFiles.set(FXCollections.observableArrayList());
        mediaPlayers.set(FXCollections.observableHashMap());
        musicFiles.addListener((ListChangeListener<File>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (File file : c.getAddedSubList()) {
                        try {
                            String url = file.toURI().toURL().toExternalForm();
                            Media media = new Media(url);
                            MediaPlayer player = new MediaPlayer(media);
                            player.setVolume(volume.get());
                            player.setMute(mute.get());
                            player.setOnEndOfMedia(() -> gotoTrack(+1));
                            mediaPlayers.put(file, player);
                            AudioFile audioFile = AudioFileIO.read(file);
                            Tag tag = audioFile.getTag();
                            Artwork artwork = tag.getFirstArtwork();
                            Image img = null;
                            if (artwork != null) {
                                byte[] data = artwork.getBinaryData();
                                if (data != null) {
                                    img = coversCache.get(data);
                                    if (img == null) {
                                        img = new Image(new ByteArrayInputStream(data));
                                        coversCache.put(data, img);
                                        Color color = predominantColorsCache.get(img);
                                        if (color == null) {
                                            BufferedImage bImg = ImageIO.read(new ByteArrayInputStream(data));
                                            int[] rgb = ColorThief.getColor(bImg, 1, false);
                                            if (rgb != null)
                                                color = new Color(rgb[0] / 255.0, rgb[1] / 255.0, rgb[2] / 255.0, 1.0);
                                            predominantColorsCache.put(img, color);
                                        }
                                    }
                                }
                                url = artwork.getImageUrl();
                                if (img == null && url != null && !url.isEmpty()) {
                                    img = coversCache.get(url);
                                    if (img == null) {
                                        img = new Image(url);
                                        coversCache.put(url, img);
                                        Color color = predominantColorsCache.get(img);
                                        if (color == null) {
                                            BufferedImage bImg = ImageIO.read(new URL(url));
                                            int[] rgb = ColorThief.getColor(bImg, 1, false);
                                            if (rgb != null)
                                                color = new Color(rgb[0] / 255.0, rgb[1] / 255.0, rgb[2] / 255.0, 0.0);
                                            predominantColorsCache.put(img, color);
                                        }
                                    }
                                }
                            } else {
                                img = emptyCoverImage;
                                Color color = predominantColorsCache.get(img);
                                if (color == null) {
                                    BufferedImage bImg = ImageIO.read(new URL(EMPTY_COVER_IMAGE_URL));
                                    int[] rgb = ColorThief.getColor(bImg, 1, false);
                                    if (rgb != null)
                                        color = new Color(rgb[0] / 255.0, rgb[1] / 255.0, rgb[2] / 255.0, 0.0);
                                    predominantColorsCache.put(img, color);
                                }
                            }
                            coversMap.put(player, img);
                        } catch (IOException | CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
                            // TODO
                        }
                    }
                } else if (c.wasRemoved()) {
                    for (File file : c.getRemoved()) {
                        MediaPlayer player = mediaPlayers.get(file);
                        if (player != null) {
                            player.dispose();
                            mediaPlayers.remove(file);
                            coversMap.remove(player);
                        }
                    }
                }
            }

            if (currentMediaPlayer.get() == null)
                gotoTrack(+1);
        });
        currentMediaPlayer.addListener(mediaPlayerChangeListener);
    }

    @Override
    public ReadOnlyIntegerProperty currentIndexProperty() {
        return currentIndex;
    }

    @Override
    public Color getPredominantColor(Image newValue) {
        return predominantColorsCache.get(newValue);
    }

    @Override
    public ObjectProperty<Image> coverImageProperty() {
        return coverImage;
    }

    @Override
    public void addFiles(List<File> files) {
        musicFiles.addAll(files.stream().filter(this::isMusicFile).collect(Collectors.toList()));
    }

    @Override
    public void seek(double value) {
        MediaPlayer player = currentMediaPlayer.get();
        if (player != null)
        {
            player.seek(Duration.millis((value / 100) * totalTime.get()));
            if (player.getStatus() != MediaPlayer.Status.PLAYING)
                currentTime.set((long) ((value / 100) * totalTime.get()));
        }
    }

    @Override
    public boolean isMusicFile(File file) {
        boolean isMusicFile = false;
        try {
            String mime = Files.probeContentType(file.toPath());
            isMusicFile = mime != null && mime.startsWith("audio/");
        } catch (IOException e) {
            // TODO
        }
        return isMusicFile;
    }

    @Override
    public ObservableList<File> getMusicFiles() {
        return musicFiles.get();
    }

    @Override
    public void togglePlayPause() {
        MediaPlayer player = currentMediaPlayer.get();
        if (player != null) {
            if (player.getStatus() == MediaPlayer.Status.PLAYING)
                player.pause();
            else
                player.play();
        }
    }

    @Override
    public void toggleRepeat() {
        int val = repeat.get();
        val++;
        repeat.set(val % 3);
    }

    @Override
    public void gotoTrack(int offset) {
        int index = currentIndex.get() + offset;
        index = index % musicFiles.getSize();
        File file = musicFiles.get(index);
        MediaPlayer player = mediaPlayers.get(file);
        currentMediaPlayer.set(player);
        currentIndex.set(index);
    }

    @Override
    public ObservableBooleanValue canGotoPreviousTrack() {
        return Bindings.isNotEmpty(musicFiles).and(currentIndex.greaterThan(-1));
    }

    @Override
    public ObservableBooleanValue canTogglePlayPause() {
        return Bindings.isNotEmpty(musicFiles).and(Bindings.isNotNull(currentMediaPlayer));
    }

    @Override
    public ObservableBooleanValue canGotoNextTract() {
        return Bindings.isNotEmpty(musicFiles).and(currentIndex.lessThan(Bindings.size(musicFiles).subtract(1)));
    }

    @Override
    public ObservableBooleanValue isLoaded() {
        return Bindings.isNotNull(currentMediaPlayer);
    }

   /* @Override
    public ObservableBooleanValue isPlaying() {
        ObjectBinding<MediaPlayer.Status> statusBinding = Bindings.select(currentMediaPlayer, "status");
        return statusBinding.isEqualTo(MediaPlayer.Status.PLAYING);
    }*/

    @Override
    public BooleanProperty isPlayingProperty() {
        return isPlaying;
    }

    @Override
    public DoubleProperty currentDurationProperty() {
        return currentDuration;
    }

    @Override
    public LongProperty totalTimeProperty() {
        return totalTime;
    }

    @Override
    public LongProperty currentTimeProperty() {
        return currentTime;
    }

    @Override
    public BooleanProperty muteProperty() {
        return mute;
    }

    @Override
    public DoubleProperty volumeProperty() {
        return volume;
    }
}
