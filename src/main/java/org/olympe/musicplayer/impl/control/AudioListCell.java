package org.olympe.musicplayer.impl.control;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.css.PseudoClass;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.olympe.musicplayer.MusicPlayerController;

import java.io.File;

public class AudioListCell extends TextFieldListCell<File> {

    private static final PseudoClass PSEUDO_CLASS_LOADED = PseudoClass.getPseudoClass("loaded");

    private final MusicPlayerController controller;

    public AudioListCell(MusicPlayerController controller, StringConverter<File> converter) {
        super(converter);
        getStyleClass().add("audio-list-cell");
        this.controller = controller;
    }

    @Override
    public void updateItem(File item, boolean empty) {
        if (loaded.isBound())
            loaded.unbind();
        super.updateItem(item, empty);
        if (!empty && item != null)
            loaded.bind(controller.createIsLoadedBindingFor(item));
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

    public static Callback<ListView<File>, ListCell<File>> forListView(MusicPlayerController controller, StringConverter<File> converter) {
        return list -> new AudioListCell(controller, converter);
    }

}
