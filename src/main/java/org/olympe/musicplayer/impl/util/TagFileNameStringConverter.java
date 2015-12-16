package org.olympe.musicplayer.impl.util;


import java.util.ResourceBundle;

public class TagFileNameStringConverter extends javafx.util.StringConverter<String> {

    private final ResourceBundle resources;

    public TagFileNameStringConverter(ResourceBundle resources) {
        super();
        this.resources = resources;
    }

    @Override
    public String toString(String object) {
        if (object != null && resources.containsKey(object)) {
            return resources.getString(object);
        }
        return object;
    }

    @Override
    public String fromString(String string) {
        return string;
    }
}
