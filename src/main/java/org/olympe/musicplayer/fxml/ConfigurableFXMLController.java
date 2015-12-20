package org.olympe.musicplayer.fxml;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;

/**
 *
 */
public abstract class ConfigurableFXMLController extends AbstractFXMLController
{
    @FXML
    private Button optionsButton;
    private ObservableList<Item> options;
    private PropertySheet sheet;

    public ConfigurableFXMLController(Application application, Stage stage)
    {
        super(application, stage);
        options = FXCollections.observableArrayList();
    }

    /**
     * Add this code at the end of the initialize to support the options in your code.
     */
    public final void collectOptions()
    {
        logger.entering("ConfigurableFXMLController", "collectOptions");
        collectOptions(options);
        logger.exiting("ConfigurableFXMLController", "collectOptions");
    }

    protected abstract void collectOptions(ObservableList<Item> options);

    protected abstract void showOptions(PropertySheet sheet);

    @Override
    void onAction(ActionEvent event)
    {
        logger.entering("ConfigurableFXMLController", "onAction", event);
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == optionsButton)
        {
            showOptions();
            event.consume();
        }
        logger.exiting("ConfigurableFXMLController", "onAction");
    }

    @Override
    void initialize()
    {
        logger.entering("ConfigurableFXMLController", "initialize");
        optionsButton.disableProperty().bind(Bindings.isEmpty(options));
        optionsButton.visibleProperty().bind(Bindings.isNotEmpty(options));
        optionsButton.managedProperty().bind(Bindings.isNotEmpty(options));
        logger.exiting("ConfigurableFXMLController", "initialize");
    }

    private void showOptions()
    {
        logger.entering("ConfigurableFXMLController", "showOptions");
        if (sheet == null)
        {
            sheet = new PropertySheet(options);
        }
        showOptions(sheet);
        logger.exiting("ConfigurableFXMLController", "showOptions");
    }
}
