package org.olympe.musicplayer.bean.configurator;

import java.util.Locale;
import java.util.prefs.Preferences;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 */
public class CoreConfigurator extends Configurator
{
    private ObjectProperty<Language> language;

    public CoreConfigurator(Preferences prefs)
    {
        super(prefs);
    }

    public ObjectProperty<Language> languageProperty()
    {
        String def = Locale.getDefault().getDisplayLanguage(Locale.ENGLISH).toUpperCase();
        Language lang = Language.valueOf(prefs.get("language", def));
        if (lang == null)
            lang = Language.ENGLISH;
        if (language == null)
            language = new SimpleObjectProperty<>(this, "language", lang);
        return language;
    }

    public Language getLanguage()
    {
        return languageProperty().get();
    }

    public void setLanguage(Language language)
    {
        languageProperty().set(language);
    }

    @Override
    public void saveToPreferences()
    {
        prefs.put("language", getLanguage().name());
    }

    public enum Language
    {
        ENGLISH(Locale.ENGLISH), FRENCH(Locale.FRANCE), GERMAN(Locale.GERMAN);
        private Locale locale;

        Language(Locale locale)
        {
            this.locale = locale;
        }

        public Locale getLocale()
        {
            return locale;
        }

        @Override
        public String toString()
        {
            String str = locale.getDisplayLanguage();
            str = str.toLowerCase();
            str = str.substring(0, 1).toUpperCase() + str.substring(1);
            return str;
        }
    }
}
