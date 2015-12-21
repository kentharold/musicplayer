package org.olympe.musicplayer.fxml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;
import static javafx.application.Platform.runLater;
import static javafx.scene.input.MouseButton.PRIMARY;

/**
 *
 */
public abstract class ListViewFXMLController<T> extends DataFXMLController<T>
{
    @FXML
    private ListView<T> dataView;

    public ListViewFXMLController(Application application, Stage stage)
    {
        super(application, stage);
    }

    @Override
    protected final SelectionModel<T> getSelectionModel()
    {
        logger.entering("ListViewFXMLController", "getSelectionModel");
        SelectionModel<T> result = dataView.getSelectionModel();
        logger.exiting("ListViewFXMLController", "getSelectionModel", result);
        return result;
    }

    protected abstract Node createPlaceholder();

    protected abstract Callback<ListView<T>, ListCell<T>> createCellFactoryFor(ListView<T> listView);

    @Override
    void initialize()
    {
        logger.entering("ListViewFXMLController", "initialize");
        super.initialize();
        dataView.setPlaceholder(createPlaceholder());
        dataView.setCellFactory(createCellFactoryFor(dataView));
        dataView.setEditable(false);
        dataView.setItems(getData());
        dataView.setFixedCellSize(Region.USE_COMPUTED_SIZE);
        dataView.setOrientation(Orientation.VERTICAL);
        dataView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        logger.entering("ListViewFXMLController", "initialize");
    }

    @Override
    void onDragOver(DragEvent event)
    {
        logger.entering("ListViewFXMLController", "onDragOver", event);
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == dataView)
        {
            Dragboard db = event.getDragboard();
            if (db.hasFiles())
                event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        }
        logger.exiting("ListViewFXMLController", "onDragOver");
    }

    @Override
    void onDragDropped(DragEvent event)
    {
        logger.entering("ListViewFXMLController", "onDragDropped", event);
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == dataView)
        {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles())
            {
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
        logger.exiting("ListViewFXMLController", "onDragDropped");
    }

    @Override
    void onMouseClicked(MouseEvent event)
    {
        logger.entering("ListViewFXMLController", "onMouseClicked", event);
        super.onMouseClicked(event);
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source instanceof ListCell)
        {
            ListCell listCell = (ListCell) source;
            ListView listView = listCell.getListView();
            if (listView == dataView && event.getButton() == PRIMARY && event.getClickCount() == 2)
            {
                runLater(() -> onAction(new ActionEvent(listCell, listCell)));
            }
        }
        logger.exiting("ListViewFXMLController", "onMouseClicked");
    }

    private List<File> filterSupportedFiles(List<File> files)
    {
        logger.entering("ListViewFXMLController", "filterSupportedFiles", files);
        List<ExtensionFilter> extFilters = new ArrayList<>();
        registerExtensionFilters(extFilters);
        ExtensionFilter extFilter = getSelectedExtensionFilter(extFilters);
        Stream<File> stream = files.stream().filter(file -> match(file, extFilter));
        List<File> filteredFiles = stream.collect(Collectors.toList());
        logger.exiting("ListViewFXMLController", "filterSupportedFiles", filteredFiles);
        return filteredFiles;
    }

    private boolean match(File file, ExtensionFilter extFilter)
    {
        logger.entering("ListViewFXMLController", "match", new Object[]{file, extFilter});
        List<String> extensions = extFilter.getExtensions();
        Stream<String> stream = extensions.stream();
        boolean result = stream.anyMatch(extension -> match(file, extension));
        logger.exiting("ListViewFXMLController", "match", result);
        return result;
    }

    private boolean match(File file, String extension)
    {
        logger.entering("ListViewFXMLController", "match", new Object[]{file, extension, false});
        String name = file.getName();
        String pattern = extension.replace("*.", ".*\\.");
        boolean result = name.matches(pattern);
        logger.exiting("ListViewFXMLController", "match", result);
        return result;
    }
}
