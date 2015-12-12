package org.olympe.musicplayer.impl.control;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.css.PseudoClass;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class AudioListCell<T> extends TextFieldListCell<T> {

    private static final PseudoClass PSEUDO_CLASS_LOADED = PseudoClass.getPseudoClass("loaded");

    public AudioListCell(StringConverter<T> converter) {
        super(converter);
    }

    // loaded
    private ReadOnlyBooleanWrapper loaded = new ReadOnlyBooleanWrapper(this, "loaded") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(PSEUDO_CLASS_LOADED, get());
        }
    };

    public ReadOnlyBooleanProperty loadedProperty() {
        return loaded.getReadOnlyProperty();
    }

    public boolean isLoaded() {
        return loaded.get();
    }

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(StringConverter<T> converter) {
        return list -> new AudioListCell(converter);
    }
}
