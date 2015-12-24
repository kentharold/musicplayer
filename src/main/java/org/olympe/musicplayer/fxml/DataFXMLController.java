package org.olympe.musicplayer.fxml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionModel;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import static javafx.stage.FileChooser.ExtensionFilter;

/**
 *
 */
public abstract class DataFXMLController<T> extends InternalNotifierFXMLController
{
    @FXML
    private Button addDataButton;
    @FXML
    private Button removeDataButton;
    private ObservableList<T> data;
    private FileChooser fileChooser;

    public DataFXMLController(Application application, Stage stage)
    {
        super(application, stage);
        data = FXCollections.observableArrayList();
    }

    public final ObservableList<T> getData()
    {
        return data;
    }

    protected abstract void fireDataRemoved(List<? extends T> datas);

    protected abstract void fireDataAdded(List<? extends T> datas);

    protected abstract SelectionModel<T> getSelectionModel();

    protected abstract T mapDataFromFile(File file);

    protected abstract ExtensionFilter getSelectedExtensionFilter(List<ExtensionFilter> extFilters);

    protected abstract void registerExtensionFilters(List<ExtensionFilter> extFilters);

    @Override
    void initialize()
    {
        logger.entering("DataFXMLController", "initialize");
        super.initialize();
        data.addListener(this::onDataListChanged);
        SelectionModel selection = getSelectionModel();
        ObservableBooleanValue isSelectionEmpty = Bindings.isNull(selection.selectedItemProperty());
        if (selection instanceof MultipleSelectionModel)
        {
            MultipleSelectionModel mSelection = (MultipleSelectionModel) selection;
            isSelectionEmpty = Bindings.isEmpty(mSelection.getSelectedItems());
        }
        removeDataButton.disableProperty().bind(isSelectionEmpty);
        removeDataButton.visibleProperty().bind(Bindings.not(isSelectionEmpty));
        removeDataButton.managedProperty().bind(Bindings.not(isSelectionEmpty));
        logger.exiting("DataFXMLController", "initialize");
    }

    @Override
    void onAction(ActionEvent event)
    {
        logger.entering("DataFXMLController", "onAction", event);
        super.onAction(event);
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == addDataButton)
        {
            if (fileChooser == null)
                fileChooser = createFileChooser();
            List<File> files = fileChooser.showOpenMultipleDialog(getStage());
            if (files != null && !files.isEmpty())
            {
                File dir = files.get(0).getParentFile();
                fileChooser.setInitialDirectory(dir);
                List<T> data = files.stream().map(this::mapDataFromFile).collect(Collectors.toList());
                this.data.addAll(data);
            }
            event.consume();
        }
        else if (source == removeDataButton)
        {
            SelectionModel<T> model = getSelectionModel();
            List<T> selectedData = new ArrayList<>();
            if (model instanceof MultipleSelectionModel)
            {
                MultipleSelectionModel<T> sModel = (MultipleSelectionModel<T>) model;
                selectedData.addAll(sModel.getSelectedItems());
            }
            else
                selectedData.add(model.getSelectedItem());
            data.removeAll(selectedData);
            model.clearSelection();
            event.consume();
        }
        logger.exiting("DataFXMLController", "onAction");
    }

    private void onDataListChanged(ListChangeListener.Change<? extends T> c)
    {
        logger.entering("DataFXMLController", "onDataListChanged", c);
        while (c.next())
        {
            if (c.wasRemoved())
            {
                fireDataRemoved(c.getRemoved());
            }
            if (c.wasAdded())
            {
                fireDataAdded(c.getAddedSubList());
            }
        }
        logger.exiting("DataFXMLController", "onDataListChanged");
    }

    private FileChooser createFileChooser()
    {
        logger.entering("DataFXMLController", "createFileChooser");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle(localize("AudioFileChooser.title"));
        List<ExtensionFilter> extFilters = fileChooser.getExtensionFilters();
        registerExtensionFilters(extFilters);
        ExtensionFilter selected = getSelectedExtensionFilter(extFilters);
        fileChooser.setSelectedExtensionFilter(selected);
        logger.entering("DataFXMLController", "createFileChooser", fileChooser);
        return fileChooser;
    }
}
