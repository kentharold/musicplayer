package org.olympe.musicplayer.impl.control;


import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class TagTableCell<S, T> extends TextFieldTableCell<S, T> {

    public TagTableCell(StringConverter<T> converter) {
        super(converter);
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(
            final StringConverter<T> converter) {
        return list -> new TagTableCell<>(converter);
    }

    @Override
    public void updateItem(T item, boolean empty) {
        setTooltip(null);
        super.updateItem(item, empty);
        if (!empty && item != null) {
            setTooltip(new Tooltip(getText()));
        }
    }
}
