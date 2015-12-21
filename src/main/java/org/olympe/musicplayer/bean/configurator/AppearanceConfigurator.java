package org.olympe.musicplayer.bean.configurator;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import jfxtras.labs.util.Util;

/**
 *
 */
public class AppearanceConfigurator
{
    private ObjectProperty<Color> color;
    private BooleanProperty useCoverPredominantColor;

    public ObjectProperty<Color> colorProperty()
    {
        if (color == null)
            color = new SimpleObjectProperty<>(this, "color", Util.webColorToColor("#deaddc"));
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
        if (useCoverPredominantColor == null)
            useCoverPredominantColor = new SimpleBooleanProperty(this, "useCoverPredominantColor", true);
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
}
