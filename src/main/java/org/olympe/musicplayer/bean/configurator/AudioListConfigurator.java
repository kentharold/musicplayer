package org.olympe.musicplayer.bean.configurator;

import java.util.prefs.Preferences;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.StringConverter;

import org.olympe.musicplayer.bean.model.Audio;
import org.olympe.musicplayer.util.AudioStringConverter;
import org.olympe.musicplayer.util.AudioStringConverter.Flag;
import static org.olympe.musicplayer.fxml.AbstractFXMLController.localize;

/**
 *
 */
public class AudioListConfigurator extends Configurator
{
    private ObjectProperty<AudioListDisplayMode> displayMode;

    public AudioListConfigurator(Preferences prefs)
    {
        super(prefs);
    }

    public ObjectProperty<AudioListDisplayMode> displayModeProperty()
    {
        if (displayMode == null)
            displayMode = new SimpleObjectProperty<>(this, "displayMode", AudioListDisplayMode.valueOf(prefs.get("displayMode", "FILE_NAME")));
        return displayMode;
    }

    public AudioListDisplayMode getDisplayMode()
    {
        return displayModeProperty().get();
    }

    public void setDisplayMode(AudioListDisplayMode displayMode)
    {
        displayModeProperty().set(displayMode);
    }

    @Override
    public void saveToPreferences()
    {
        prefs.put("displayMode", getDisplayMode().name());
    }

    public enum AudioListDisplayMode
    {
        FILE_NAME(new AudioStringConverter()),
        TRACK_TITLE(new AudioStringConverter(Flag.TRACK_TITLE)),
        TRACK_ARTIST_TITLE(new AudioStringConverter(Flag.TRACK_ARTIST, Flag.SEPARATOR_FLAG, Flag.TRACK_TITLE));
        private ReadOnlyObjectWrapper<StringConverter<Audio>> converter;

        AudioListDisplayMode(StringConverter<Audio> converter)
        {
            this.converter = new ReadOnlyObjectWrapper<>(this, "converter", converter);
        }

        public StringConverter<Audio> getConverter()
        {
            return converter.get();
        }

        public ReadOnlyObjectProperty<StringConverter<Audio>> converterReadOnlyProperty()
        {
            return converter.getReadOnlyProperty();
        }

        @Override
        public String toString()
        {
            return localize(name());
        }
    }
}
