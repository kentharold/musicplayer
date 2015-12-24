package org.olympe.musicplayer.util;

import java.util.Optional;

import javafx.beans.value.ObservableValue;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.BeanProperty;
import org.controlsfx.property.editor.PropertyEditor;

import static org.olympe.musicplayer.fxml.AbstractFXMLController.localize;

/**
 *
 */
public class BeanPropertyWrapper implements Item
{
    private final BeanProperty delegateBeanProperty;
    private final String category;

    public BeanPropertyWrapper(Item property)
    {
        this.delegateBeanProperty = (BeanProperty) property;
        Object bean = delegateBeanProperty.getBean();
        String className = bean.getClass().getSimpleName();
        category = className.replaceAll("Configurator", "");
    }

    @Override
    public Class<?> getType()
    {
        return delegateBeanProperty.getType();
    }

    @Override
    public String getCategory()
    {
        return localize("Configurator." + category + ".name");
    }

    @Override
    public String getName()
    {
        return localize("Configurator." + category + "." + delegateBeanProperty.getName() + ".name");
    }

    @Override
    public String getDescription()
    {
        return localize("Configurator." + category + "." + delegateBeanProperty.getDescription() + ".description");
    }

    @Override
    public Object getValue()
    {
        return delegateBeanProperty.getValue();
    }

    @Override
    public void setValue(Object value)
    {
        delegateBeanProperty.setValue(value);
    }

    @Override
    public Optional<ObservableValue<?>> getObservableValue()
    {
        return delegateBeanProperty.getObservableValue();
    }

    @Override
    public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass()
    {
        return delegateBeanProperty.getPropertyEditorClass();
    }

    @Override
    public boolean isEditable()
    {
        return delegateBeanProperty.isEditable();
    }
}
