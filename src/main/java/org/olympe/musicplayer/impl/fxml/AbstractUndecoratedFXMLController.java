package org.olympe.musicplayer.impl.fxml;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * This controller abstraction allows the end user
 * to toggle the window to full screen, minimize,
 * maximize, resize or close it.
 */
public abstract class AbstractUndecoratedFXMLController extends AbstractFXMLController {

    @FXML
    protected Button fullscreenButton;
    @FXML
    protected Button minimizeButton;
    @FXML
    protected Button maximizeButton;
    @FXML
    protected Button closeButton;
    @FXML
    protected HBox titlebarHbox;

    private double mouseDragOffsetX;
    private double mouseDragOffsetY;

    public AbstractUndecoratedFXMLController(Application application, Stage stage) {
        super(application, stage);
    }

    @Override
    void onAction(ActionEvent event) {
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        Stage stage = getStage();
        if (source == fullscreenButton) {
            stage.setFullScreen(!stage.isFullScreen());
        } else if (source == minimizeButton) {
            stage.setIconified(!stage.isIconified());
        } else if (source == maximizeButton) {
            stage.setMaximized(!stage.isMaximized());
        } else if (source == closeButton) {
            stage.close();
            Platform.exit();
        } else {
            return;
        }
        event.consume();
    }

    @Override
    void onMousePressed(MouseEvent event) {
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == titlebarHbox) {
            mouseDragOffsetX = event.getSceneX();
            mouseDragOffsetY = event.getScreenY();
        } else {
            return;
        }
        event.consume();
    }

    @Override
    void onMouseDragged(MouseEvent event) {
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        Stage stage = getStage();
        if (source == titlebarHbox) {
            stage.setX(event.getScreenX() - mouseDragOffsetX);
            stage.setY(event.getScreenY() - mouseDragOffsetY);
        } else {
            return;
        }
        event.consume();
    }
}
