package org.olympe.musicplayer.fxml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
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

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

import org.olympe.musicplayer.bean.configurator.AudioListConfigurator;
import org.olympe.musicplayer.bean.configurator.AudioListConfigurator.AudioListDisplayMode;
import org.olympe.musicplayer.bean.model.Audio;
import org.olympe.musicplayer.util.BeanPropertyWrapper;

/**
 *
 */
public abstract class AudioListFXMLController extends ListViewFXMLController<Audio>
{
    private final AudioListConfigurator configurator = new AudioListConfigurator();

    public AudioListFXMLController(Application application, Stage stage)
    {
        super(application, stage);
    }

    @Override
    protected final Audio mapDataFromFile(File file)
    {
        try
        {
            return new Audio(file);
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, String.format("can not add file \"%s\"", file), e);
        }
        return null;
    }

    @Override
    protected final void registerExtensionFilters(List<ExtensionFilter> extFilters)
    {
        String description = localize("ExtensionFilter.description");
        List<String> extensions = new ArrayList<>();
        registerAudioExtensions(extensions);
        ExtensionFilter extFilter = new ExtensionFilter(description, extensions);
        extFilters.add(extFilter);
    }

    @Override
    protected final ExtensionFilter getSelectedExtensionFilter(List<ExtensionFilter> extFilters)
    {
        return extFilters.get(0);
    }

    @Override
    protected final Callback<ListView<Audio>, ListCell<Audio>> createCellFactoryFor(ListView<Audio> listView)
    {
        return this::createListCell;
    }

    @Override
    protected final void fireDataAdded(List<? extends Audio> addedDatas)
    {
        addedDatas.stream().forEach(this::registerAudio);
    }

    @Override
    protected void collectOptions(ObservableList<PropertySheet.Item> options)
    {
        super.collectOptions(options);
        Stream<PropertySheet.Item> stream = BeanPropertyUtils.getProperties(configurator).stream();
        stream = stream.map(BeanPropertyWrapper::new);
        options.addAll(stream.collect(Collectors.toList()));
    }

    protected void unregisterMediaPlayer(File file, MediaPlayer mediaPlayer)
    {
        // TODO
    }

    protected void registerMediaPlayer(File file, MediaPlayer mediaPlayer)
    {
        // TODO
    }

    @Override
    protected Node createPlaceholder()
    {
        String msg = localize("DataView.PlaceHolder.msg");
        Label msgLabel = new Label(msg);
        msgLabel.setWrapText(true);
        return new StackPane(msgLabel);
    }

    @Override
    protected void fireDataRemoved(List<? extends Audio> datas)
    {
        datas.stream().forEach(this::unregisterAudio);
    }

    protected void unregisterAudio(Audio audio)
    {
        // TODO
    }

    protected void registerAudio(Audio audio)
    {
        // TODO
    }

    protected void registerAudioExtensions(List<String> extensions)
    {
        extensions.add("*.mp3");
    }

    @Override
    void initialize()
    {
        super.initialize();
        Audio.getMediaPlayerCache().addListener(this::onMediaPlayerCacheChanged);
    }

    private ListCell<Audio> createListCell(ListView<Audio> listView)
    {
        TextFieldListCell<Audio> listCell = new TextFieldListCell<>(configurator.getDisplayMode().getConverter());
        configurator.displayModeProperty().addListener((observable1, oldValue, newValue) -> updateCell(listCell, newValue));
        listCell.setOnMouseClicked(this::onMouseClicked);
        return listCell;
    }

    private void updateCell(TextFieldListCell<Audio> listCell, AudioListDisplayMode newValue)
    {
        listCell.setConverter(newValue.getConverter());
        listCell.updateItem(listCell.getItem(), listCell.isEmpty());
    }

    private void onMediaPlayerCacheChanged(MapChangeListener.Change<? extends File, ? extends MediaPlayer> c)
    {
        if (c.wasRemoved())
            unregisterMediaPlayer(c.getKey(), c.getValueRemoved());
        if (c.wasAdded())
            registerMediaPlayer(c.getKey(), c.getValueAdded());
    }
}
