package org.olympe.musicplayer.fxml;

import java.beans.PropertyDescriptor;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
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
    private Preferences rootPrefs;

    public ConfigurableFXMLController(Application application, Stage stage)
    {
        super(application, stage);
        options = FXCollections.observableArrayList();
        rootPrefs = Preferences.userRoot().node("olympe/musicplayer");
    }

    public final Preferences getPreferencesNode(String node)
    {
        return rootPrefs.node(node);
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

    public boolean isValidProperty(PropertyDescriptor propertyDescriptor)
    {
        boolean valid = false;
        if (propertyDescriptor != null)
        {
            String name = propertyDescriptor.getName();
            if (!name.equals("prefs"))
                valid = true;
        }
        return valid;
    }

    protected abstract void collectOptions(ObservableList<Item> options);

    protected abstract void showOptions(PropertySheet sheet);

    protected abstract BooleanProperty optionsViewVisible();

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
        optionsButton.visibleProperty().bind(Bindings.isNotEmpty(options).and(optionsViewVisible().not()));
        optionsButton.managedProperty().bind(Bindings.isNotEmpty(options).and(optionsViewVisible().not()));
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
