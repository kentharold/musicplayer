package org.olympe.musicplayer.impl.fxml;

import javafx.application.Application;
import javafx.collections.MapChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.olympe.musicplayer.Audio;
import org.olympe.musicplayer.impl.util.AudioStringConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 */
public abstract class AudioListFXMLController extends ListViewFXMLController<Audio> {

    public AudioListFXMLController(Application application, Stage stage) {
        super(application, stage);
    }

    @Override
    void initialize() {
        super.initialize();
        Audio.getMediaPlayerCache().addListener(this::onMediaPlayerCacheChanged);
    }

    private void onMediaPlayerCacheChanged(MapChangeListener.Change<? extends File, ? extends MediaPlayer> c) {
        if (c.wasRemoved())
            unregisterMediaPlayer(c.getKey(), c.getValueRemoved());
        if (c.wasAdded())
            registerMediaPlayer(c.getKey(), c.getValueAdded());
    }

    protected void unregisterMediaPlayer(File file, MediaPlayer mediaPlayer) {
        // TODO
    }

    protected void registerMediaPlayer(File file, MediaPlayer mediaPlayer) {
        // TODO
    }

    @Override
    protected final Audio mapDataFromFile(File file) {
        try {
            return new Audio(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, String.format("can not add file \"%s\"", file), e);
        }
        return null;
    }

    @Override
    protected final void registerExtensionFilters(List<ExtensionFilter> extFilters) {
        String description = getResources().getString("ExtensionFilter.description");
        List<String> extensions = new ArrayList<>();
        registerAudioExtensions(extensions);
        ExtensionFilter extFilter = new ExtensionFilter(description, extensions);
        extFilters.add(extFilter);
    }

    @Override
    protected final ExtensionFilter getSelectedExtensionFilter(List<ExtensionFilter> extFilters) {
        return extFilters.get(0);
    }

    @Override
    protected final Callback<ListView<Audio>, ListCell<Audio>> createCellFactoryFor(ListView<Audio> listView) {
        return param -> {
            TextFieldListCell<Audio> listCell = new TextFieldListCell<>();
            listCell.setConverter(new AudioStringConverter());
            listCell.setOnMouseClicked(this::onMouseClicked);
            return listCell;
        };
    }

    @Override
    protected Node createPlaceholder() {
        String msg = getResources().getString("DataView.PlaceHolder.msg");
        Label msgLabel = new Label(msg);
        msgLabel.setWrapText(true);
        return new StackPane(msgLabel);
    }

    @Override
    protected final void fireDataAdded(List<? extends Audio> addedDatas) {
        addedDatas.stream().forEach(this::registerAudio);
    }

    @Override
    protected void fireDataRemoved(List<? extends Audio> datas) {
        datas.stream().forEach(this::unregisterAudio);
    }

    protected void unregisterAudio(Audio audio) {
        // TODO
    }


    protected void registerAudio(Audio audio) {
        // TODO
    }

    protected void registerAudioExtensions(List<String> extensions) {
        extensions.add("*.mp3");
    }
}
