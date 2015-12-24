package org.olympe.musicplayer.fxml;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 *
 */
public abstract class AbstractExtraFXMLController extends CoverImageFXMLController
{
    @FXML
    private TabPane tabView;

    public AbstractExtraFXMLController(Application application, Stage stage)
    {
        super(application, stage);
    }

    @Override
    void initialize()
    {
        super.initialize();
    }
}
