package org.olympe.musicplayer.bean.configurator;

import java.util.prefs.Preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 */
public class WindowConfigurator extends Configurator
{
    private BooleanProperty rememberWindowState;

    public WindowConfigurator(Preferences prefs)
    {
        super(prefs);
    }

    public BooleanProperty rememberWindowStateProperty()
    {
        if (rememberWindowState == null)
            rememberWindowState = new SimpleBooleanProperty(this, "rememberWindowState", prefs.getBoolean("rememberWindowState", true));
        return rememberWindowState;
    }

    public boolean getRememberWindowState()
    {
        return rememberWindowStateProperty().get();
    }

    public void setRememberWindowState(boolean rememberWindowState)
    {
        rememberWindowStateProperty().set(rememberWindowState);
    }

    @Override
    public void saveToPreferences()
    {
        prefs.putBoolean("rememberWindowState", getRememberWindowState());
    }
}
