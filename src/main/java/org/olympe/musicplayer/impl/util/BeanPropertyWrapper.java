package org.olympe.musicplayer.impl.util;

import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.BeanProperty;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.Optional;

import static org.olympe.musicplayer.impl.fxml.AbstractFXMLController.localize;

/**
 *
 */
public class BeanPropertyWrapper implements Item {

    private final BeanProperty delegateBeanProperty;

    public BeanPropertyWrapper(Item property) {
        this.delegateBeanProperty = (BeanProperty) property;
    }

    @Override
    public Class<?> getType() {
        return delegateBeanProperty.getType();
    }

    @Override
    public String getCategory() {
        return localize(delegateBeanProperty.getBean().getClass().getSimpleName() + ".name");
    }

    @Override
    public String getName() {
        return localize(delegateBeanProperty.getName() + ".name");
    }

    @Override
    public String getDescription() {
        return localize(delegateBeanProperty.getDescription() + ".description");
    }

    @Override
    public Object getValue() {
        return delegateBeanProperty.getValue();
    }

    @Override
    public void setValue(Object value) {
        delegateBeanProperty.setValue(value);
    }

    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return delegateBeanProperty.getObservableValue();
    }

    @Override
    public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
        return delegateBeanProperty.getPropertyEditorClass();
    }

    @Override
    public boolean isEditable() {
        return delegateBeanProperty.isEditable();
    }
}
