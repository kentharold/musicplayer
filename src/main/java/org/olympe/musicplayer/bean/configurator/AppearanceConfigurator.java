package org.olympe.musicplayer.bean.configurator;

import java.util.prefs.Preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import jfxtras.labs.util.Util;

/**
 *
 */
public class AppearanceConfigurator extends Configurator
{
    private ObjectProperty<Color> color;
    private BooleanProperty useCoverPredominantColor;

    public AppearanceConfigurator(Preferences prefs)
    {
        super(prefs);
    }

    public ObjectProperty<Color> colorProperty()
    {
        if (color == null)
            color = new SimpleObjectProperty<>(this, "color", Util.webColorToColor(prefs.get("color", "#deaddc")));
        return color;
    }

    public Color getColor()
    {
        return colorProperty().get();
    }

    public void setColor(Color color)
    {
        colorProperty().set(color);
    }

    public BooleanProperty useCoverPredominantColorProperty()
    {
        String key = "useCoverPredominantColor";
        if (useCoverPredominantColor == null)
            useCoverPredominantColor = new SimpleBooleanProperty(this, key, prefs.getBoolean(key, true));
        return useCoverPredominantColor;
    }

    public boolean getUseCoverPredominantColor()
    {
        return useCoverPredominantColorProperty().get();
    }

    public void setUseCoverPredominantColor(boolean useCoverPredominantColor)
    {
        useCoverPredominantColorProperty().set(useCoverPredominantColor);
    }

    @Override
    public void saveToPreferences()
    {
        prefs.put("color", Util.colorToWebColor(getColor()));
        prefs.putBoolean("useCoverPredominantColor", getUseCoverPredominantColor());
    }
}
