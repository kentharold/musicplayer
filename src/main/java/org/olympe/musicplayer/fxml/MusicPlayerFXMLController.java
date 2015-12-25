package org.olympe.musicplayer.fxml;

import java.io.File;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import org.olympe.musicplayer.bean.configurator.PlayerConfigurator;
import org.olympe.musicplayer.bean.model.Audio;
import org.olympe.musicplayer.util.BeanPropertyWrapper;

/**
 *
 */
public abstract class MusicPlayerFXMLController extends AbstractMusicPlayerFXMLController
{
    @FXML
    private Button prevButton;
    @FXML
    private ToggleButton playToggleButton;
    @FXML
    private Button nextButton;
    @FXML
    private Slider durationSlider;
    @FXML
    private ToggleButton muteToggleButton;
    @FXML
    private Slider volumeSlider;
    @FXML
    private CheckBox repeatCheckBox;
    @FXML
    private Label currentTimeLabel;
    @FXML
    private Label totalTimeLabel;
    private BooleanProperty currentDurationChangingInternally;
    private ChangeListener<Duration> currentTimeChangeListener;
    private ChangeListener<Duration> totalTimeChangeListener;
    private ChangeListener<? super Number> currentProgressChangeListener;
    private PlayerConfigurator configurator;

    public MusicPlayerFXMLController(Application application, Stage stage)
    {
        super(application, stage);
        configurator = new PlayerConfigurator(getPreferencesNode("player"));
        addExitHandler(configurator::saveToPreferences);
        currentDurationChangingInternally = new SimpleBooleanProperty();
        totalTimeChangeListener = this::fireTotalTimeChanged;
        currentProgressChangeListener = this::fireCurrentProgressChanged;
        currentTimeChangeListener = this::currentTimeChanged;
        addExitHandler(this::savePlayerState);
    }

    @Override
    protected final void onEndOfMedia()
    {
        logger.entering("MusicPlayerFXMLController", "onEndOfMedia");
        if (computeRepeat() != 1)
        {
            stop();
            load(+1);
            MediaPlayer mediaPlayer = getLoadedMediaPlayer();
            if (isPlaySelected())
                mediaPlayer.play();
        }
        logger.exiting("MusicPlayerFXMLController", "onEndOfMedia");
    }

    @Override
    protected final int compute(int offset)
    {
        logger.entering("MusicPlayerFXMLController", "compute", offset);
        if (getData().isEmpty())
            return -1;
        int index = getLoadedIndex();
        if (index == -1)
            index = 0;
        index += offset;
        index = index % getData().size();
        if (offset < 0 && index < 0)
            index += getData().size();
        logger.exiting("MusicPlayerFXMLController", "compute", index);
        return index;
    }

    @Override
    protected final boolean isPlaySelected()
    {
        return playToggleButton.isSelected();
    }

    @Override
    protected final void setPlaySelected(boolean b)
    {
        playToggleButton.setSelected(b);
    }

    @Override
    protected final BooleanProperty muteProperty()
    {
        return muteToggleButton.selectedProperty();
    }

    @Override
    protected final DoubleProperty volumeProperty()
    {
        return volumeSlider.valueProperty();
    }

    @Override
    protected final ToggleButton getMusicPlayerButton()
    {
        return playToggleButton;
    }

    @Override
    protected int computeRepeat()
    {
        logger.entering("MusicPlayerFXMLController", "computeRepeat");
        int repeat = -1;
        if (!repeatCheckBox.isSelected() && !repeatCheckBox.isIndeterminate())
            repeat = 0;
        else if (repeatCheckBox.isSelected())
            repeat = 1;
        else if (repeatCheckBox.isIndeterminate())
            repeat = 2;
        assert repeat == 0 || repeat == 1 || repeat == 2;
        logger.exiting("MusicPlayerFXMLController", "computeRepeat", repeat);
        return repeat;
    }

    @Override
    protected void updateAudio(ObservableValue<? extends Audio> observable, Audio oldValue, Audio newValue)
    {
        logger.entering("MusicPlayerFXMLController", "updateAudio", new Object[]{observable, oldValue, newValue});
        if (oldValue != null)
        {
            MediaPlayer player = oldValue.getMediaPlayer();
            player.currentTimeProperty().removeListener(currentTimeChangeListener);
            player.totalDurationProperty().removeListener(totalTimeChangeListener);
        }
        currentProgressProperty().set(0.0);
        currentDurationProperty().set(0);
        if (newValue != null)
        {
            MediaPlayer player = newValue.getMediaPlayer();
            player.currentTimeProperty().addListener(currentTimeChangeListener);
            player.cycleDurationProperty().addListener(totalTimeChangeListener);
            if (player.getTotalDuration() != null)
            {
                totalDurationProperty().set((long) player.getTotalDuration().toMillis());
            }
        }
        logger.exiting("MusicPlayerFXMLController", "updateAudio");
    }

    @Override
    protected void collectOptions(ObservableList<PropertySheet.Item> options)
    {
        logger.entering("MusicPlayerFXMLController", "collectOptions", options);
        super.collectOptions(options);
        Stream<PropertySheet.Item> stream = BeanPropertyUtils.getProperties(configurator, this::isValidProperty).stream();
        stream = stream.map(BeanPropertyWrapper::new);
        options.addAll(stream.collect(Collectors.toList()));
        logger.exiting("MusicPlayerFXMLController", "collectOptions");
    }

    @Override
    void initialize()
    {
        logger.entering("MusicPlayerFXMLController", "initialize");
        super.initialize();
        BooleanBinding isDataEmpty = Bindings.isEmpty(getData());
        BooleanBinding isNoAudioLoaded = Bindings.valueAt(getData(), loadedIndexProperty()).isNull();
        prevButton.disableProperty().bind(isDataEmpty.or(isNoAudioLoaded));
        playToggleButton.disableProperty().bind(isDataEmpty.or(isNoAudioLoaded));
        nextButton.disableProperty().bind(isDataEmpty.or(isNoAudioLoaded));
        durationSlider.disableProperty().bind(isNoAudioLoaded);
        currentTimeLabel.disableProperty().bind(isNoAudioLoaded);
        currentTimeLabel.visibleProperty().bind(isNoAudioLoaded.not());
        currentTimeLabel.managedProperty().bind(isNoAudioLoaded.not());
        totalTimeLabel.disableProperty().bind(isNoAudioLoaded);
        totalTimeLabel.visibleProperty().bind(isNoAudioLoaded.not());
        totalTimeLabel.managedProperty().bind(isNoAudioLoaded.not());
        currentTimeLabel.textProperty().bind(Bindings.format("%1$tM:%1$tS", currentDurationProperty()));
        totalTimeLabel.textProperty().bind(Bindings.format("%1$tM:%1$tS", totalDurationProperty()));
        durationSlider.valueProperty().bindBidirectional(currentProgressProperty());
        currentProgressProperty().addListener(currentProgressChangeListener);
        Tooltip tooltip = prevButton.getTooltip();
        StringBinding audioStr = createAudioStringBinding(Bindings.valueAt(getData(), loadedIndexProperty().subtract(1)));
        tooltip.textProperty().bind(Bindings.format(localize("MediaControl.Prev.descriptionFmt"), audioStr));
        tooltip = playToggleButton.getTooltip();
        GlyphIcon tooltipGraphic = (FontAwesomeIconView) tooltip.getGraphic();
        GlyphIcon graphic = (FontAwesomeIconView) playToggleButton.getGraphic();
        tooltipGraphic.glyphNameProperty().bind(graphic.glyphNameProperty());
        audioStr = createAudioStringBinding(loadedAudioProperty());
        tooltip.textProperty().bind(Bindings.when(playToggleButton.selectedProperty()).then(Bindings.format(localize("MediaControl.Pause.descriptionFmt"), audioStr)).otherwise(Bindings.format(localize("MediaControl.Play.descriptionFmt"), audioStr)));
        tooltip = nextButton.getTooltip();
        audioStr = createAudioStringBinding(Bindings.valueAt(getData(), loadedIndexProperty().subtract(-1)));
        tooltip.textProperty().bind(Bindings.format(localize("MediaControl.Next.descriptionFmt"), audioStr));
        tooltip = currentTimeLabel.getTooltip();
        tooltip.textProperty().bind(currentTimeLabel.textProperty());
        tooltip = totalTimeLabel.getTooltip();
        tooltip.textProperty().bind(totalTimeLabel.textProperty());
        tooltip = muteToggleButton.getTooltip();
        tooltipGraphic = (MaterialDesignIconView) tooltip.getGraphic();
        graphic = (MaterialDesignIconView) muteToggleButton.getGraphic();
        tooltipGraphic.glyphNameProperty().bind(graphic.glyphNameProperty());
        tooltip.textProperty().bind(Bindings.when(muteProperty()).
                then(localize("MediaControl.Unmute.description")).otherwise(localize("MediaControl.Mute.description")));
        tooltip = repeatCheckBox.getTooltip();
        tooltipGraphic = (MaterialDesignIconView) tooltip.getGraphic();
        graphic = (MaterialDesignIconView) repeatCheckBox.getGraphic();
        tooltipGraphic.glyphNameProperty().bind(graphic.glyphNameProperty());
        audioStr = createAudioStringBinding(loadedAudioProperty());
        StringBinding b = Bindings.when(loadedAudioProperty().isNull()).then(localize("MediaControl.RepeatEmptyTrack.description")).otherwise(Bindings.format(localize("MediaControl.RepeatTrack.descriptionFmt"), audioStr));
        tooltip.textProperty().bind(Bindings.when(repeatCheckBox.selectedProperty()).then(localize("MediaControl.DisableRepeat.description")).otherwise(Bindings.when(repeatCheckBox.indeterminateProperty()).then(b).otherwise(localize("MediaControl.RepeatPlayQueue.description"))));
        Platform.runLater(this::restorePlayerState);
        logger.exiting("MusicPlayerFXMLController", "initialize");
    }

    @Override
    void onMouseMoved(MouseEvent event)
    {
        super.onMouseMoved(event);
        if (!event.isConsumed())
        {
            Object source = event.getSource();
            if (source instanceof Slider)
            {
                double value = -1;
                Slider slider = (Slider) source;
                NumberAxis axis = (NumberAxis) slider.lookup(".axis");
                StackPane track = (StackPane) slider.lookup(".track");
                StackPane thumb = (StackPane) slider.lookup(".thumb");
                boolean useAxis = slider.isShowTickLabels() || slider.isShowTickMarks();
                if (useAxis)
                {
                    // James: use axis to convert value/position
                    Point2D locationInAxis = axis.sceneToLocal(event.getSceneX(), event.getSceneY());
                    boolean isHorizontal = slider.getOrientation() == Orientation.HORIZONTAL;
                    double mouseX = isHorizontal ? locationInAxis.getX() : locationInAxis.getY();
                    value = axis.getValueForDisplay(mouseX).doubleValue();
                }
                else
                {
                    // this can't work because we don't know the internals of the track
                    Point2D locationInAxis = track.sceneToLocal(event.getSceneX(), event.getSceneY());
                    double mouseX = locationInAxis.getX();
                    double trackLength = track.getWidth();
                    double percent = mouseX / trackLength;
                    value = slider.getMin() + ((slider.getMax() - slider.getMin()) * percent);
                }
                if (value >= slider.getMin() && value <= slider.getMax())
                {
                    Tooltip tooltip = slider.getTooltip();
                    tooltip.setText(null);
                    String key = null;
                    if (slider == volumeSlider)
                    {
                        key = "MediaControl.Volume.descriptionFmt";
                        value *= 100;
                        tooltip.setText(String.format(localize(key), value));
                    }
                    else if (slider == durationSlider)
                    {
                        key = "MediaControl.Duration.descriptionFmt";
                        long time = (long) ((value / 100) * totalDurationProperty().get());
                        tooltip.setText(String.format(localize(key), time));
                        // FIXME: 24.12.2015 The time display is not the same when cliked on it.
                    }
                    if (key != null)
                    {
                        event.consume();
                    }
                }
            }
        }
    }

    @Override
    void onAction(ActionEvent event)
    {
        logger.entering("MusicPlayerFXMLController", "onAction", event);
        super.onAction(event);
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == prevButton)
        {
            stepBackward();
            event.consume();
        }
        else if (source == playToggleButton)
        {
            togglePlay();
            event.consume();
        }
        else if (source == nextButton)
        {
            stepForward();
            event.consume();
        }
        else if (source == muteToggleButton)
        {
            toggleMute();
            event.consume();
        }
        else if (source == repeatCheckBox)
        {
            toggleRepeat();
            event.consume();
        }
        logger.entering("MusicPlayerFXMLController", "onAction");
    }

    private void savePlayerState()
    {
        Preferences prefs = configurator.getPrefs();
        if (configurator.getRememberPlayerState())
        {
            prefs.putDouble("currentTime", durationSlider.getValue());
            prefs.putDouble("volume", volumeSlider.getValue());
            prefs.putBoolean("mute", muteToggleButton.isSelected());
            prefs.putBoolean("repeatSelected", repeatCheckBox.isSelected());
            prefs.putBoolean("repeatIndeterminate", repeatCheckBox.isIndeterminate());
            prefs.putInt("currentIndex", getLoadedIndex());
            Stream<Audio> audios = getData().stream();
            Stream<File> files = audios.map(Audio::getFile);
            Stream<String> pathNames = files.map(File::getAbsolutePath);
            String dataStr = String.join(File.pathSeparator, pathNames.collect(Collectors.toList()));
            prefs.put("data", dataStr);
        }
    }

    private void restorePlayerState()
    {
        if (configurator.getRememberPlayerState())
        {
            Preferences prefs = configurator.getPrefs();
            volumeSlider.setValue(prefs.getDouble("volume", 0.5));
            muteToggleButton.setSelected(prefs.getBoolean("mute", false));
            repeatCheckBox.setSelected(prefs.getBoolean("repeatSelected", false));
            repeatCheckBox.setIndeterminate(prefs.getBoolean("repeatIndeterminate", false));
            String dataStr = prefs.get("data", null);
            if (dataStr != null && !dataStr.isEmpty())
            {
                Stream<String> pathNames = Stream.of(dataStr.split(File.pathSeparator));
                Stream<File> files = pathNames.map(File::new);
                Stream<Audio> audios = files.map(this::mapDataFromFile);
                getData().addAll(audios.collect(Collectors.toList()));
                int index = prefs.getInt("currentIndex", -1);
                if (index >= 0 && index < getData().size())
                {
                    Audio audio = getData().get(index);
                    loadedIndexProperty().set(index);
                    loadedAudioProperty().set(audio);
                    // currentProgressProperty().set(prefs.getDouble("currentTime", 0.0));
                }
            }
        }
    }

    private void currentTimeChanged(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue)
    {
        logger.entering("MusicPlayerFXMLController", "currentTimeChanged", new Object[]{observableValue, oldValue, newValue});
        if (newValue != null)
        {
            long currentMillis = (long) newValue.toMillis();
            currentDurationProperty().set(currentMillis);
            currentDurationChangingInternally.set(true);
            double duration = (newValue.toMillis() / totalDurationProperty().get()) * 100;
            currentProgressProperty().set(duration);
            currentDurationChangingInternally.set(false);
        }
        logger.exiting("MusicPlayerFXMLController", "currentTimeChanged");
    }

    private void fireTotalTimeChanged(ObservableValue<? extends Duration> obs, Duration oldValue, Duration newValue)
    {
        logger.entering("MusicPlayerFXMLController", "fireTotalTimeChanged", new Object[]{obs, oldValue, newValue});
        if (newValue != null)
        {
            totalDurationProperty().set((long) newValue.toMillis());
        }
        logger.exiting("MusicPlayerFXMLController", "fireTotalTimeChanged");
    }

    private void fireCurrentProgressChanged(ObservableValue<? extends Number> obs, Number oldValue, Number newValue)
    {
        logger.entering("MusicPlayerFXMLController", "fireCurrentProgressChanged", new Object[]{obs, oldValue, newValue});
        if (newValue != null && !currentDurationChangingInternally.get())
        {
            seek(newValue.doubleValue());
        }
        logger.exiting("MusicPlayerFXMLController", "fireCurrentProgressChanged");
    }
}
