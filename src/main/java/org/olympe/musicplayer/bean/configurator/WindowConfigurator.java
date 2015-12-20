package org.olympe.musicplayer.bean.configurator;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 */
public class WindowConfigurator
{
    private BooleanProperty rememberWindowState;

    public BooleanProperty rememberWindowStateProperty()
    {
        if (rememberWindowState == null)
            rememberWindowState = new SimpleBooleanProperty(this, "rememberWindowState", true);
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
}
