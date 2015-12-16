package org.olympe.musicplayer;

import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.olympe.musicplayer.impl.util.MusicFileTag;

import java.io.File;
import java.util.List;

public interface MusicPlayerController {

    // Methods

    void addFiles(List<File> files);

    boolean isMusicFile(File file);

    ObservableList<File> getMusicFiles();

    void togglePlayPause();

    void toggleRepeat();

    void gotoTrack(int i);

    Color getPredominantColor(Image newValue);

    void seek(double value);

    // Properties & Bindings

    ObservableBooleanValue canGotoPreviousTrack();

    ObservableBooleanValue canTogglePlayPause();

    ObservableBooleanValue canGotoNextTract();

    ObservableBooleanValue isLoaded();

    BooleanProperty muteProperty();

    BooleanProperty isPlayingProperty();

    LongProperty totalTimeProperty();

    LongProperty currentTimeProperty();

    DoubleProperty currentDurationProperty();

    DoubleProperty volumeProperty();

    ObservableValue<? extends Image> coverImageProperty();

    ReadOnlyIntegerProperty currentIndexProperty();

    ObservableBooleanValue createIsLoadedBindingFor(File item);

    ObjectProperty<MusicFileTag> musicFileTagProperty();
}
