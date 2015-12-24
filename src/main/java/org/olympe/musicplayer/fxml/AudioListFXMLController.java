package org.olympe.musicplayer.fxml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import static javafx.scene.media.MediaPlayer.Status.PLAYING;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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
    private HashMap<Audio, ToggleButton> graphicCache;
    private HashMap<ToggleButton, Audio> graphicCacheInv;
    private ToggleGroup playingAudio = new ToggleGroup();
    private ObjectProperty<Audio> loadedAudio;

    public AudioListFXMLController(Application application, Stage stage)
    {
        super(application, stage);
        configurator = new AudioListConfigurator(getPreferencesNode("view/playqueue"));
        addExitHandler(configurator::saveToPreferences);
        graphicCache = new HashMap<>();
        graphicCacheInv = new HashMap<>();
        loadedAudio = new SimpleObjectProperty<>(this, "loadedAudio");
        loadedAudio.addListener(this::updateAudio);
    }

    protected static final StringBinding createAudioStringBinding(ObservableValue<Audio> loadedAudio)
    {
        return new StringBinding()
        {

            {
                super.bind(loadedAudio);
            }

            @Override
            public void dispose()
            {
                super.unbind(loadedAudio);
            }

            @Override
            public ObservableList<ObservableValue<?>> getDependencies()
            {
                return FXCollections.<ObservableValue<?>>singletonObservableList(loadedAudio);
            }

            @Override
            protected String computeValue()
            {
                final Audio value = loadedAudio.getValue();
                StringConverter<Audio> converter = AudioListDisplayMode.FILE_NAME.getConverter();
                return (value == null) ? null : converter.toString(value);
            }
        };
    }

    public final Audio getLoadedAudio()
    {
        return loadedAudio.get();
    }

    public final ObjectProperty<Audio> loadedAudioProperty()
    {
        return loadedAudio;
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
        Audio audio = getLoadedAudio();
        if (audio != null)
        {
            ToggleButton toggleButton = graphicCache.get(audio);
            toggleButton.setSelected(play);
        }
        logger.exiting("AbstractMusicPlayerFXMLController", "setPlay");
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

    public final MediaPlayer getLoadedMediaPlayer()
    {
        Audio audio = getLoadedAudio();
        MediaPlayer player = null;
        if (audio != null)
            player = audio.getMediaPlayer();
        return player;
    }

    public final void stop()
    {
        logger.entering("AbstractMusicPlayerFXMLController", "stop");
        MediaPlayer mediaPlayer = getLoadedMediaPlayer();
        if (mediaPlayer != null)
            mediaPlayer.stop();
        logger.exiting("AbstractMusicPlayerFXMLController", "stop");
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
        String description = localize("AudioFileChooser.ExtensionFilter.description");
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

    public abstract void load(Audio audio);

    protected abstract void updateAudio(ObservableValue<? extends Audio> observable, Audio oldValue, Audio newValue);

    @Override
    protected void collectOptions(ObservableList<PropertySheet.Item> options)
    {
        logger.entering("AudioListFXMLController", "collectOptions", options);
        super.collectOptions(options);
        Stream<PropertySheet.Item> stream = BeanPropertyUtils.getProperties(configurator, this::isValidProperty).stream();
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

    protected void unregisterAudio(Audio audio)
    {
        ToggleButton toggleButton = graphicCache.remove(audio);
        graphicCacheInv.remove(toggleButton);
        toggleButton.setToggleGroup(null);
    }

    protected void registerAudio(Audio audio)
    {
        ToggleButton toggleButton = new ToggleButton(null, new FontAwesomeIconView());
        toggleButton.setToggleGroup(playingAudio);
        toggleButton.getStyleClass().add("glyph-button");
        toggleButton.setOnAction(this::onAction);
        graphicCache.put(audio, toggleButton);
        graphicCacheInv.put(toggleButton, audio);
    }

    protected abstract ToggleButton getMusicPlayerButton();

    protected void registerAudioExtensions(List<String> extensions)
    {
        logger.entering("AudioListFXMLController", "registerAudioExtensions", extensions);
        extensions.add("*.mp3");
        logger.exiting("AudioListFXMLController", "registerAudioExtensions");
    }

    protected abstract void setPlaySelected(boolean b);

    @Override
    void onAction(ActionEvent event)
    {
        super.onAction(event);
        if (!event.isConsumed())
        {
            Object source = event.getSource();
            if (source instanceof ToggleButton && graphicCache.values().contains(source))
            {
                ToggleButton toggleButton = (ToggleButton) source;
                Audio audio = graphicCacheInv.get(toggleButton);
                boolean isLoaded = loadedAudio.get() == audio;
                stop();
                ToggleButton playButton = getMusicPlayerButton();
                if (!isLoaded)
                    load(audio);
                playButton.fire();
            }
            else if (source instanceof ListCell)
            {
                ListCell listCell = (ListCell) source;
                Object obj = listCell.getItem();
                if (obj instanceof Audio)
                {
                    stop();
                    Audio audio = (Audio) obj;
                    ToggleButton toggleButton = graphicCache.get(audio);
                    toggleButton.fire();
                    event.consume();
                }
            }
        }
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
        StringConverter<Audio> converter = configurator.getDisplayMode().getConverter();
        TextFieldListCell<Audio> listCell = new AudioListCell();
        listCell.setConverter(converter);
        configurator.displayModeProperty().addListener((observable1, oldValue, newValue) -> updateCell(listCell, newValue));
        listCell.setOnMouseClicked(this::onMouseClicked);
        listCell.getStyleClass().setAll("audio-list-cell");
        listCellWidthUpdater(listCell);
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

    public class AudioListCell extends TextFieldListCell<Audio>
    {
        private final PseudoClass PSEUDO_CLASS_LOADED = PseudoClass.getPseudoClass("loaded");
        private BooleanProperty loaded = new SimpleBooleanProperty()
        {

            @Override
            public Object getBean()
            {
                return this;
            }

            @Override
            public String getName()
            {
                return "loaded";
            }

            @Override
            protected void invalidated()
            {
                pseudoClassStateChanged(PSEUDO_CLASS_LOADED, get());
            }
        };

        public AudioListCell()
        {
            loaded.bind(loadedAudio.isEqualTo(itemProperty()));
        }

        @Override
        public void updateItem(Audio item, boolean empty)
        {
            super.updateItem(item, empty);
            if (item == null || empty)
                setGraphic(null);
            else
            {
                setGraphic(graphicCache.get(item));
            }
        }
    }
}
