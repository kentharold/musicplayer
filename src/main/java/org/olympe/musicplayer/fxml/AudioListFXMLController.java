package org.olympe.musicplayer.fxml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private final AudioListConfigurator configurator;

    public AudioListFXMLController(Application application, Stage stage)
    {
        super(application, stage);
        configurator = new AudioListConfigurator(getPreferencesNode("view/playqueue"));
        addExitHandler(configurator::saveToPreferences);
    }

    @Override
    protected final Audio mapDataFromFile(File file)
    {
        logger.entering("AudioListFXMLController", "mapDataFromFile", file);
        Audio result = null;
        try
        {
            result = new Audio(file);
        }
        catch (IOException e)
        {
            logger.warning(e.getLocalizedMessage());
        }
        logger.exiting("AudioListFXMLController", "mapDataFromFile", result);
        return result;
    }

    @Override
    protected final void registerExtensionFilters(List<ExtensionFilter> extFilters)
    {
        logger.entering("AudioListFXMLController", "registerExtensionFilters", extFilters);
        String description = localize("ExtensionFilter.description");
        List<String> extensions = new ArrayList<>();
        registerAudioExtensions(extensions);
        ExtensionFilter extFilter = new ExtensionFilter(description, extensions);
        extFilters.add(extFilter);
        logger.exiting("AudioListFXMLController", "registerExtensionFilters");
    }

    @Override
    protected final ExtensionFilter getSelectedExtensionFilter(List<ExtensionFilter> extFilters)
    {
        logger.entering("AudioListFXMLController", "getSelectedExtensionFilter", extFilters);
        ExtensionFilter result = extFilters.get(0);
        logger.exiting("AudioListFXMLController", "getSelectedExtensionFilter", result);
        return result;
    }

    @Override
    protected final Callback<ListView<Audio>, ListCell<Audio>> createCellFactoryFor(ListView<Audio> listView)
    {
        logger.entering("AudioListFXMLController", "createCellFactoryFor", listView);
        Callback<ListView<Audio>, ListCell<Audio>> result = this::createListCell;
        logger.exiting("AudioListFXMLController", "createCellFactoryFor", result);
        return result;
    }

    @Override
    protected final void fireDataAdded(List<? extends Audio> addedDatas)
    {
        logger.entering("AudioListFXMLController", "fireDataAdded", addedDatas);
        addedDatas.stream().forEach(this::registerAudio);
        logger.exiting("AudioListFXMLController", "fireDataAdded");
    }

    @Override
    protected void collectOptions(ObservableList<PropertySheet.Item> options)
    {
        logger.entering("AudioListFXMLController", "collectOptions", options);
        super.collectOptions(options);
        Stream<PropertySheet.Item> stream = BeanPropertyUtils.getProperties(configurator).stream();
        stream = stream.map(BeanPropertyWrapper::new);
        options.addAll(stream.collect(Collectors.toList()));
        logger.exiting("AudioListFXMLController", "collectOptions");
    }

    protected abstract void unregisterMediaPlayer(File file, MediaPlayer mediaPlayer);

    protected abstract void registerMediaPlayer(File file, MediaPlayer mediaPlayer);

    @Override
    protected Node createPlaceholder()
    {
        logger.entering("AudioListFXMLController", "createPlaceholder");
        String msg = localize("DataView.PlaceHolder.msg");
        Label msgLabel = new Label(msg);
        msgLabel.setWrapText(true);
        Node placeHolder = new StackPane(msgLabel);
        logger.exiting("AudioListFXMLController", "createPlaceholder");
        return placeHolder;
    }

    @Override
    protected void fireDataRemoved(List<? extends Audio> datas)
    {
        logger.entering("AudioListFXMLController", "fireDataRemoved", datas);
        datas.stream().forEach(this::unregisterAudio);
        logger.exiting("AudioListFXMLController", "fireDataRemoved");
    }

    protected abstract void unregisterAudio(Audio audio);

    protected abstract void registerAudio(Audio audio);

    protected void registerAudioExtensions(List<String> extensions)
    {
        logger.entering("AudioListFXMLController", "registerAudioExtensions", extensions);
        extensions.add("*.mp3");
        logger.exiting("AudioListFXMLController", "registerAudioExtensions");
    }

    @Override
    void initialize()
    {
        logger.entering("AudioListFXMLController", "initialize");
        super.initialize();
        Audio.getMediaPlayerCache().addListener(this::onMediaPlayerCacheChanged);
        logger.exiting("AudioListFXMLController", "initialize");
    }

    private ListCell<Audio> createListCell(ListView<Audio> listView)
    {
        logger.entering("AudioListFXMLController", "createListCell", listView);
        TextFieldListCell<Audio> listCell = new TextFieldListCell<>(configurator.getDisplayMode().getConverter());
        configurator.displayModeProperty().addListener((observable1, oldValue, newValue) -> updateCell(listCell, newValue));
        listCell.setOnMouseClicked(this::onMouseClicked);
        logger.entering("AudioListFXMLController", "createListCell", listCell);
        return listCell;
    }

    private void updateCell(TextFieldListCell<Audio> listCell, AudioListDisplayMode newValue)
    {
        logger.entering("AudioListFXMLController", "updateCell", new Object[]{listCell, newValue});
        listCell.setConverter(newValue.getConverter());
        listCell.updateItem(listCell.getItem(), listCell.isEmpty());
        logger.exiting("AudioListFXMLController", "updateCell");
    }

    private void onMediaPlayerCacheChanged(MapChangeListener.Change<? extends File, ? extends MediaPlayer> c)
    {
        logger.entering("AudioListFXMLController", "onMediaPlayerCacheChanged", c);
        if (c.wasRemoved())
            unregisterMediaPlayer(c.getKey(), c.getValueRemoved());
        if (c.wasAdded())
            registerMediaPlayer(c.getKey(), c.getValueAdded());
        logger.exiting("AudioListFXMLController", "onMediaPlayerCacheChanged");
    }
}
