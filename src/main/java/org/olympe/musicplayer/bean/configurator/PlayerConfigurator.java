package org.olympe.musicplayer.bean.configurator;

import java.util.prefs.Preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 */
public class PlayerConfigurator extends Configurator
{
    private BooleanProperty rememberPlayerState;

    public PlayerConfigurator(Preferences prefs)
    {
        super(prefs);
    }

    public final BooleanProperty rememberPlayerStateProperty()
    {
        if (rememberPlayerState == null)
            rememberPlayerState = new SimpleBooleanProperty(this, "rememberPlayerState", prefs.getBoolean("rememberPlayerState", true));
        return rememberPlayerState;
    }

    public boolean getRememberPlayerState()
    {
        return rememberPlayerStateProperty().get();
    }

    public void setRememberPlayerState(boolean rememberPlayerState)
    {
        rememberPlayerStateProperty().set(rememberPlayerState);
    }

    @Override
    public void saveToPreferences()
    {
        prefs.putBoolean("rememberPlayerState", getRememberPlayerState());
    }
}
