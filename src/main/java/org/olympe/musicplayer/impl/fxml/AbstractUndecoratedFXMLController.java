package org.olympe.musicplayer.impl.fxml;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * This controller abstraction allows the end user
 * to toggle the window to full screen, minimize,
 * maximize, resize, move or close it.
 */
public abstract class AbstractUndecoratedFXMLController extends AbstractFXMLController {

    @FXML
    private Button fullscreenButton;
    @FXML
    private Button minimizeButton;
    @FXML
    private Button maximizeButton;
    @FXML
    private Button closeButton;
    @FXML
    private HBox titlebarHbox;
    @FXML
    private Parent root;

    private double mouseDragOffsetX;
    private double mouseDragOffsetY;
    private BoundingBox savedBounds;
    private boolean maximized = false;

    public AbstractUndecoratedFXMLController(Application application, Stage stage) {
        super(application, stage);
    }

    @Override
    void onAction(ActionEvent event) {
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == fullscreenButton) {
            toggleFullScreen();
            event.consume();
        } else if (source == minimizeButton) {
            toggleMinimized();
            event.consume();
        } else if (source == maximizeButton) {
            toggleMaximized();
            event.consume();
        } else if (source == closeButton) {
            exit(0);
            event.consume(); // should never reach hier.
        }
    }

    @Override
    void onMousePressed(MouseEvent event) {
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == titlebarHbox) {
            mouseDragOffsetX = event.getSceneX();
            mouseDragOffsetY = event.getScreenY();
            event.consume();
        }
    }

    @Override
    void onMouseDragged(MouseEvent event) {
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        Stage stage = getStage();
        if (stage.isFullScreen() || stage.isMaximized())
            return;
        if (source == titlebarHbox) {
            stage.setX(event.getScreenX() - mouseDragOffsetX);
            stage.setY(event.getScreenY() - mouseDragOffsetY);
            event.consume();
        }
    }

    @Override
    void onMouseClicked(MouseEvent event) {
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        Stage stage = getStage();
        if (source == titlebarHbox && !stage.isFullScreen()) {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                toggleMaximized();
                event.consume();
            }
        }
    }

    @Override
    void onMouseMoved(MouseEvent event) {
    }

    //
    // Maximized state of the window.
    //

    public final void setMaximized(boolean maximized) {
        Stage stage = getStage();
        if (!maximized) {
            stage.setX(savedBounds.getMinX());
            stage.setY(savedBounds.getMinY());
            stage.setWidth(savedBounds.getWidth());
            stage.setHeight(savedBounds.getHeight());
            savedBounds = null;
            this.maximized = false;
        } else {
            Rectangle2D rect = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(rect);
            Screen screen = screensForRectangle.get(0);
            Rectangle2D visualBounds = screen.getVisualBounds();
            savedBounds = new BoundingBox(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            stage.setX(visualBounds.getMinX());
            stage.setY(visualBounds.getMinY());
            stage.setWidth(visualBounds.getWidth());
            stage.setHeight(visualBounds.getHeight());
            this.maximized = true;
        }
    }

    public final boolean isMaximized() {
        return maximized;
    }

    public final void toggleMaximized() {
        setMaximized(!isMaximized());
    }

    //
    // Minimized state of the window.
    //

    public final void setMinimized(boolean minimized) {
        getStage().setIconified(minimized);
    }

    public final boolean isMinimized() {
        return getStage().isIconified();
    }

    public final void toggleMinimized() {
        setMinimized(!isMinimized());
    }

    //
    // Full screen state of the window.
    //

    public final void setFullScreen(boolean fullScreen) {
        getStage().setFullScreen(fullScreen);
    }

    public final boolean isFullScreen() {
        return getStage().isFullScreen();
    }

    public final void toggleFullScreen() {
        setFullScreen(!isFullScreen());
    }

    //
    // Quit the application.
    //

    public final void exit(int status) {
        getStage().close();
        Platform.exit();
        System.exit(status);
    }
}
