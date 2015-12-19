package org.olympe.musicplayer.impl.fxml;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.input.*;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public abstract class ListViewFXMLController<T> extends DataFXMLController<T> {

    @FXML
    private ListView<T> dataView;

    public ListViewFXMLController(Application application, Stage stage) {
        super(application, stage);
    }

    @Override
    void initialize() {
        super.initialize();
        dataView.setPlaceholder(createPlaceholder());
        dataView.setCellFactory(createCellFactoryFor(dataView));
        dataView.setEditable(isEditable());
        dataView.setItems(getData());
        dataView.setFixedCellSize(getFixedCellSize());
        dataView.setOrientation(getOrientation());
        dataView.getSelectionModel().setSelectionMode(getSelectionMode());
    }

    @Override
    protected final SelectionModel<T> getSelectionModel() {
        return dataView.getSelectionModel();
    }

    @Override
    void onDragOver(DragEvent event) {
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == dataView) {
            Dragboard db = event.getDragboard();
            if (db.hasFiles())
                event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        }
    }

    @Override
    void onDragDropped(DragEvent event) {
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == dataView) {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = new ArrayList<>(db.getFiles());
                logger.info(String.format("%s files dropped in the data view", files.size()));
                files = filterSupportedFiles(files);
                logger.info(String.format("adding %s audio files to data view", files.size()));
                getData().addAll(files.stream().map(this::mapDataFromFile).collect(Collectors.toList()));
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        }
    }

    @Override
    void onMouseClicked(MouseEvent event) {
        super.onMouseClicked(event);
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source instanceof ListCell) {
            ListCell<T> listCell = (ListCell<T>) source;
            ListView<T> listView = listCell.getListView();
            if (listView == dataView && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                onAction(new ActionEvent(listCell, listCell));
            }
        }
    }

    private List<File> filterSupportedFiles(List<File> files) {
        List<ExtensionFilter> extFilters = new ArrayList<>();
        registerExtensionFilters(extFilters);
        ExtensionFilter extFilter = getSelectedExtensionFilter(extFilters);
        return files.stream().filter(file -> match(file, extFilter)).collect(Collectors.toList());
    }

    private boolean match(File file, ExtensionFilter extFilter) {
        List<String> extensions = extFilter.getExtensions();
        return extensions.stream().anyMatch(extension -> match(file, extension, false));
    }

    private boolean match(File file, String extension, boolean abs) {
        String name = file.getName();
        if (abs)
            name = file.getAbsolutePath();
        String pattern = extension.replace("*.", ".*\\.");
        return name.matches(pattern);
    }

    protected SelectionMode getSelectionMode() {
        return SelectionMode.MULTIPLE;
    }

    protected Orientation getOrientation() {
        return Orientation.VERTICAL;
    }

    protected double getFixedCellSize() {
        return Region.USE_COMPUTED_SIZE;
    }

    protected boolean isEditable() {
        return false;
    }

    protected Node createPlaceholder() {
        return null;
    }

    protected abstract Callback<ListView<T>, ListCell<T>> createCellFactoryFor(ListView<T> listView);
}
