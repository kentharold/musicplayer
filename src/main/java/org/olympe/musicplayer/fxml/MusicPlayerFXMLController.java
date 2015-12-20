package org.olympe.musicplayer.fxml;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.olympe.musicplayer.bean.model.Audio;

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

    public MusicPlayerFXMLController(Application application, Stage stage)
    {
        super(application, stage);
        currentDurationChangingInternally = new SimpleBooleanProperty();
        totalTimeChangeListener = this::fireTotalTimeChanged;
        currentProgressChangeListener = this::fireCurrentProgressChanged;
        currentTimeChangeListener = this::currentTimeChanged;
    }

    @Override
    protected final void onEndOfMedia()
    {
        logger.entering("MusicPlayerFXMLController", "onEndOfMedia");
        if (computeRepeat() != 1)
        {
            stop();
            step(+1);
            MediaPlayer mediaPlayer = getLoadedAudio();
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
        logger.exiting("MusicPlayerFXMLController", "initialize");
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
        else if (source instanceof ListCell)
        {
            ListCell listCell = (ListCell) source;
            Object obj = listCell.getItem();
            if (obj instanceof Audio)
            {
                stop();
                Audio audio = (Audio) obj;
                step(audio);
                if (isPlaySelected())
                    audio.getMediaPlayer().play();
            }
            event.consume();
        }
        logger.entering("MusicPlayerFXMLController", "onAction");
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
