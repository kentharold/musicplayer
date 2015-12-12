package org.olympe.musicplayer;

import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.List;

public interface MusicPlayerController {

    void addFiles(List<File> files);

    boolean isMusicFile(File file);

    ObservableList<File> getMusicFiles();

    void togglePlayPause();

    void toggleRepeat();

    void gotoTrack(int i);

    ObservableBooleanValue canGotoPreviousTrack();

    ObservableBooleanValue canTogglePlayPause();

    ObservableBooleanValue canGotoNextTract();

    // ObservableBooleanValue isPlaying();

    DoubleProperty currentDurationProperty();

    LongProperty totalTimeProperty();

    LongProperty currentTimeProperty();

    BooleanProperty muteProperty();

    DoubleProperty volumeProperty();

    void seek(double value);

    ObservableValue<? extends Image> coverImageProperty();

    Color getPredominantColor(Image newValue);

    ObservableBooleanValue isLoaded();

    BooleanProperty isPlayingProperty();

    ReadOnlyIntegerProperty currentIndexProperty();

    ObservableBooleanValue createIsLoadedBindingFor(File item);
}
