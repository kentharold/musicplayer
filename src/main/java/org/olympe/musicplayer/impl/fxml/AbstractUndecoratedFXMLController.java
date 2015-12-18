package org.olympe.musicplayer.impl.fxml;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

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

    private boolean isResizeCursor(Cursor cursor) {
        List<Cursor> cursors = Arrays.asList(Cursor.N_RESIZE, Cursor.NE_RESIZE,
                Cursor.NW_RESIZE, Cursor.S_RESIZE, Cursor.SW_RESIZE, Cursor.SE_RESIZE,
                Cursor.W_RESIZE, Cursor.E_RESIZE);
        return cursors.contains(cursor);
    }

    @Override
    void onMousePressed(MouseEvent event) {
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == root) {
            Cursor cursor = root.getCursor();
            if (!isResizeCursor(cursor))
                return;
        }
        if ((source == titlebarHbox || source == root) && event.isPrimaryButtonDown()) {
            mouseDragOffsetX = event.getScreenX();
            mouseDragOffsetY = event.getScreenY();
            event.consume();
        } else {
            mouseDragOffsetX = -1;
            mouseDragOffsetY = -1;
        }
    }

    @Override
    void onMouseDragged(MouseEvent event) {
        if (event.isConsumed())
            return;
        Stage stage = getStage();
        Object source = event.getSource();
        if (source == titlebarHbox && !isResizeCursor(root.getCursor()) && event.isPrimaryButtonDown() && mouseDragOffsetX != -1 && mouseDragOffsetY != -1 && !isFullScreen()) {
            if (isMaximized()) {
                setMaximized(false);
                stage.setX(event.getScreenX() - stage.getWidth() / 2);
                stage.setY(event.getScreenY());
            }
            double newX = event.getScreenX();
            double newY = event.getScreenY();
            double x = stage.getX() + newX - mouseDragOffsetX;
            double y = stage.getY() + newY - mouseDragOffsetY;
            mouseDragOffsetX = newX;
            mouseDragOffsetY = newY;
            stage.setX(x);
            stage.setY(y);
            event.consume();
        } else if (source == root && event.isPrimaryButtonDown() && mouseDragOffsetX != -1 && mouseDragOffsetY != -1 && !isFullScreen() && !isMaximized()) {
            Cursor cursor = root.getCursor();
            if (cursor == Cursor.DEFAULT || event.isStillSincePress())
                return;
            boolean north = false;
            boolean south = false;
            boolean east = false;
            boolean west = false;
            if (cursor == Cursor.N_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.NW_RESIZE)
                north = true;
            if (cursor == Cursor.S_RESIZE || cursor == Cursor.SE_RESIZE || cursor == Cursor.SW_RESIZE)
                south = true;
            if (cursor == Cursor.E_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.SE_RESIZE)
                east = true;
            if (cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.SW_RESIZE)
                west = true;

            double width = stage.getWidth() + (west ? -1 : +1) * (event.getScreenX() - mouseDragOffsetX);
            double height = stage.getHeight() + (north ? -1 : +1) * (event.getScreenY() - mouseDragOffsetY);
            double x = stage.getX() + event.getScreenX() - mouseDragOffsetX;
            double y = stage.getY() + event.getScreenY() - mouseDragOffsetY;
            if ((north || south) && height >= stage.getMinHeight())
                stage.setHeight(height);
            if ((east || west) && width >= stage.getMinWidth())
                stage.setWidth(width);
            if (west && x >= 0)
                stage.setX(x);
            if (north && y >= 0) {
                Rectangle2D rect = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
                ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(rect);
                if (screensForRectangle.size() > 0) {
                    Screen screen = screensForRectangle.get(0);
                    Rectangle2D visualBounds = screen.getVisualBounds();
                    if (y < visualBounds.getHeight() - 30 && y >= visualBounds.getMinY()) {
                        stage.setY(y);
                    }
                }
            }

            mouseDragOffsetX = event.getScreenX();
            mouseDragOffsetY = event.getScreenY();

            event.consume();
        }
    }

    @Override
    void onMouseClicked(MouseEvent event) {
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        Stage stage = getStage();
        if (source == titlebarHbox && !isResizeCursor(root.getCursor()) && !stage.isFullScreen()) {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                toggleMaximized();
                event.consume();
            }
        }
    }

    @Override
    void onMouseMoved(MouseEvent event) {
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == root && !isMaximized() && !isFullScreen()) {
            // change the cursor for resize.
            root.setCursor(Cursor.DEFAULT);
            double width = getStage().getWidth();
            double height = getStage().getHeight();
            double x = event.getSceneX() - width;
            double y = event.getSceneY() - height;
            boolean north = y <= -height + 3 && y >= -height;
            boolean east = x <= 0 && x >= -3;
            boolean south = y <= 0 && y >= -3;
            boolean west = x <= -width + 3 && x >= -width;
            if (north && east)
                root.setCursor(Cursor.NE_RESIZE);
            else if (north && west)
                root.setCursor(Cursor.NW_RESIZE);
            else if (south && east)
                root.setCursor(Cursor.SE_RESIZE);
            else if (south && west)
                root.setCursor(Cursor.SW_RESIZE);
            else if (north)
                root.setCursor(Cursor.N_RESIZE);
            else if (south)
                root.setCursor(Cursor.S_RESIZE);
            else if (east)
                root.setCursor(Cursor.E_RESIZE);
            else if (west) {
                root.setCursor(Cursor.W_RESIZE);
            } else {
                return;
            }
            event.consume();
        }
    }

    //
    // Maximized state of the window.
    //

    public final boolean isMaximized() {
        return maximized;
    }

    public final void setMaximized(boolean maximized) {
        Stage stage = getStage();
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> setMaximized(maximized));
            return;
        }
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

    public final void toggleMaximized() {
        setMaximized(!isMaximized());
    }

    //
    // Minimized state of the window.
    //

    public final boolean isMinimized() {
        return getStage().isIconified();
    }

    public final void setMinimized(boolean minimized) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> setMinimized(minimized));
            return;
        }
        getStage().setIconified(minimized);
    }

    public final void toggleMinimized() {
        setMinimized(!isMinimized());
    }

    //
    // Full screen state of the window.
    //

    public final boolean isFullScreen() {
        return getStage().isFullScreen();
    }

    public final void setFullScreen(boolean fullScreen) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> setFullScreen(fullScreen));
            return;
        }
        getStage().setFullScreen(fullScreen);
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
