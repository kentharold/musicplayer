package org.olympe.musicplayer.impl.fxml;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.olympe.musicplayer.Audio;

/**
 *
 */
public abstract class MusicPlayerFXMLController extends AbstractMusicPlayerFXMLController {

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

    private BooleanProperty currentDurationChangingInternally = new SimpleBooleanProperty();
    private ChangeListener<Duration> currentTimeChangeListener = (observable, oldValue, newValue) -> {
        if (newValue != null) {
            long currentMillis = (long) newValue.toMillis();
            currentDurationProperty().set(currentMillis);
            currentDurationChangingInternally.set(true);
            double duration = (newValue.toMillis() / totalDurationProperty().get()) * 100;
            currentProgressProperty().set(duration);
            currentDurationChangingInternally.set(false);
        }
    };
    private ChangeListener<Duration> totalTimeChangeListener = (observable, oldValue, newValue) -> {
        if (newValue != null) {
            totalDurationProperty().set((long) newValue.toMillis());
        }
    };
    private ChangeListener<? super Number> currentProgressChangeListener = (observable, oldValue, newValue) -> {
        if (newValue != null && !currentDurationChangingInternally.get()) {
            seek(newValue.doubleValue());
        }
    };

    public MusicPlayerFXMLController(Application application, Stage stage) {
        super(application, stage);
    }

    @Override
    void initialize() {
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
    }

    @Override
    void onAction(ActionEvent event) {
        super.onAction(event);
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == prevButton) {
            stepBackward();
            event.consume();
        } else if (source == playToggleButton) {
            togglePlay();
            event.consume();
        } else if (source == nextButton) {
            stepForward();
            event.consume();
        } else if (source == muteToggleButton) {
            toggleMute();
            event.consume();
        } else if (source == repeatCheckBox) {
            toggleRepeat();
            event.consume();
        } else if (source instanceof ListCell) {
            ListCell listCell = (ListCell) source;
            Object obj = listCell.getItem();
            if (obj instanceof Audio) {
                Audio audio = (Audio) obj;
                step(audio);
                event.consume();
            }
        }
    }

    @Override
    protected final void onEndOfMedia() {
        if (computeRepeat() != 1) {
            stop();
            step(+1);
            MediaPlayer mediaPlayer = getLoadedMediaPlayer();
            if (isPlaySelected())
                mediaPlayer.play();
        }
    }

    @Override
    protected int computeRepeat() {
        int repeat = -1;
        if (!repeatCheckBox.isSelected() && !repeatCheckBox.isIndeterminate())
            repeat = 0;
        else if (repeatCheckBox.isSelected())
            repeat = 1;
        else if (repeatCheckBox.isIndeterminate())
            repeat = 2;

        if (repeat == -1)
            throw new IllegalStateException();

        return repeat;
    }

    @Override
    protected void updateMediaPlayer(MediaPlayer oldValue, MediaPlayer newValue) {
        if (oldValue != null) {
            oldValue.currentTimeProperty().removeListener(currentTimeChangeListener);
            oldValue.totalDurationProperty().removeListener(totalTimeChangeListener);
        }
        currentProgressProperty().set(0.0);
        currentDurationProperty().set(0);
        if (newValue != null) {
            newValue.currentTimeProperty().addListener(currentTimeChangeListener);
            newValue.cycleDurationProperty().addListener(totalTimeChangeListener);

            if (newValue.getTotalDuration() != null) {
                totalDurationProperty().set((long) newValue.getTotalDuration().toMillis());
            }
        }
    }

    @Override
    protected final int compute(int offset) {
        if (getData().isEmpty())
            return -1;
        int index = getLoadedIndex();
        if (index == -1)
            return 0;
        index += offset;
        index = index % getData().size();
        if (offset < 0 && index < 0)
            index += getData().size();
        return index;
    }

    @Override
    protected final boolean isPlaySelected() {
        return playToggleButton.isSelected();
    }

    @Override
    protected final void setPlaySelected(boolean b) {
        playToggleButton.setSelected(b);
    }

    @Override
    protected final BooleanProperty muteProperty() {
        return muteToggleButton.selectedProperty();
    }

    @Override
    protected final DoubleProperty volumeProperty() {
        return volumeSlider.valueProperty();
    }
}
