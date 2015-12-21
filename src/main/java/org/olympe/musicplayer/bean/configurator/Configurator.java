package org.olympe.musicplayer.bean.configurator;

import java.util.prefs.Preferences;

/**
 *
 */
public abstract class Configurator
{
    protected Preferences prefs;

    public Configurator(Preferences prefs)
    {
        this.prefs = prefs;
    }

    public Preferences getPrefs()
    {
        return prefs;
    }

    public abstract void saveToPreferences();
}
