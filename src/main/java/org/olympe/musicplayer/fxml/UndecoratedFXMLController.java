package org.olympe.musicplayer.fxml;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import javafx.stage.StageStyle;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.BeanPropertyUtils;

import org.olympe.musicplayer.bean.configurator.WindowConfigurator;
import org.olympe.musicplayer.util.BeanPropertyWrapper;
import static com.sun.javafx.PlatformUtil.isWin7OrLater;

/**
 * <p> This controller abstraction allows the end user to toggle the window to full screen, minimize, maximize, resize,
 * move or close it. </p>
 */
public abstract class UndecoratedFXMLController extends ConfigurableFXMLController
{
    private static List<Cursor> RESIZE_CURSORS;
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
    private WindowConfigurator configurator;
    private List<Runnable> exitHandlers = new ArrayList<>();

    public UndecoratedFXMLController(Application application, Stage stage)
    {
        super(application, stage);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(true);
        configurator = new WindowConfigurator(getPreferencesNode("view/window"));
        addExitHandler(configurator::saveToPreferences);
        addExitHandler(this::saveWindowState);
        Platform.runLater(this::restoreWindowState);
    }

    private static boolean isResizeCursor(Cursor cursor)
    {
        logger.entering("UndecoratedFXMLController", "isResizeCursor", cursor);
        if (RESIZE_CURSORS == null)
        {
            RESIZE_CURSORS = new ArrayList<>();
            RESIZE_CURSORS.add(Cursor.N_RESIZE);
            RESIZE_CURSORS.add(Cursor.NE_RESIZE);
            RESIZE_CURSORS.add(Cursor.NW_RESIZE);
            RESIZE_CURSORS.add(Cursor.S_RESIZE);
            RESIZE_CURSORS.add(Cursor.SE_RESIZE);
            RESIZE_CURSORS.add(Cursor.SW_RESIZE);
            RESIZE_CURSORS.add(Cursor.E_RESIZE);
            RESIZE_CURSORS.add(Cursor.W_RESIZE);
        }
        boolean result = RESIZE_CURSORS.contains(cursor);
        logger.exiting("UndecoratedFXMLController", "isResizeCursor", result);
        return result;
    }

    public final void addExitHandler(Runnable exitHandler)
    {
        exitHandlers.add(exitHandler);
    }

    public final void removeExitHandler(Runnable exitHandler)
    {
        exitHandlers.remove(exitHandler);
    }

    public final boolean isMaximized()
    {
        return maximized;
    }

    public final void setMaximized(boolean maximized)
    {
        logger.entering("UndecoratedFXMLController", "setMaximized", maximized);
        if (this.maximized != maximized)
        {
            Stage stage = getStage();
            if (!Platform.isFxApplicationThread())
            {
                Platform.runLater(() -> setMaximized(maximized));
                return;
            }
            if (!maximized)
            {
                applySavedBounds();
                this.maximized = false;
            }
            else
            {
                double x = stage.getX();
                double y = stage.getY();
                double width = stage.getWidth();
                double height = stage.getHeight();
                memorizeBounds();
                Rectangle2D rect = new Rectangle2D(x, y, width, height);
                ObservableList<Screen> screensForRectangle;
                screensForRectangle = Screen.getScreensForRectangle(rect);
                Screen screen = screensForRectangle.get(0);
                Rectangle2D visualBounds = screen.getVisualBounds();
                stage.setX(visualBounds.getMinX());
                stage.setY(visualBounds.getMinY());
                stage.setWidth(visualBounds.getWidth());
                stage.setHeight(visualBounds.getHeight());
                this.maximized = true;
            }
        }
        logger.exiting("UndecoratedFXMLController", "setMaximized");
    }

    public final void toggleMaximized()
    {
        logger.entering("UndecoratedFXMLController", "toggleMaximized");
        setMaximized(!isMaximized());
        logger.exiting("UndecoratedFXMLController", "toggleMaximized");
    }

    public final boolean isMinimized()
    {
        return getStage().isIconified();
    }

    public final void setMinimized(boolean minimized)
    {
        if (!Platform.isFxApplicationThread())
        {
            Platform.runLater(() -> setMinimized(minimized));
            return;
        }
        getStage().setIconified(minimized);
    }

    public final void toggleMinimized()
    {
        setMinimized(!isMinimized());
    }

    public final boolean isFullScreen()
    {
        return getStage().isFullScreen();
    }

    public final void setFullScreen(boolean fullScreen)
    {
        if (!Platform.isFxApplicationThread())
        {
            Platform.runLater(() -> setFullScreen(fullScreen));
            return;
        }
        getStage().setFullScreen(fullScreen);
    }

    public final void toggleFullScreen()
    {
        logger.entering("UndecoratedFXMLController", "toggleFullScreen");
        setFullScreen(!isFullScreen());
        logger.exiting("UndecoratedFXMLController", "toggleFullScreen");
    }

    public final void exit(int status)
    {
        logger.entering("UndecoratedFXMLController", "exit", status);
        exitHandlers.stream().forEach(Runnable::run);
        getStage().close();
        Platform.exit();
        System.exit(status);
        logger.exiting("UndecoratedFXMLController", "exit");
    }

    @Override
    protected void collectOptions(ObservableList<Item> options)
    {
        logger.entering("UndecoratedFXMLController", "collectOptions", options);
        Stream<Item> stream = BeanPropertyUtils.getProperties(configurator, this::isValidProperty).stream();
        stream = stream.map(BeanPropertyWrapper::new);
        options.addAll(stream.collect(Collectors.toList()));
        logger.exiting("UndecoratedFXMLController", "collectOptions");
    }

    @Override
    void initialize()
    {
        super.initialize();
        maximizeButton.disableProperty().bind(getStage().fullScreenProperty());
        maximizeButton.managedProperty().bind(getStage().fullScreenProperty().not());
        maximizeButton.visibleProperty().bind(getStage().fullScreenProperty().not());
    }

    @Override
    void onAction(ActionEvent event)
    {
        logger.entering("UndecoratedFXMLController", "onAction", event);
        super.onAction(event);
        if (!event.isConsumed())
        {
            Object source = event.getSource();
            boolean consume = true;
            if (source == fullscreenButton)
            {
                Stage stage = getStage();
                if (!isFullScreen())
                {
                    memorizeBounds();
                }
                toggleFullScreen();
                if (!isFullScreen())
                {
                    Platform.runLater(this::applySavedBounds);
                }
            }
            else if (source == minimizeButton)
                toggleMinimized();
            else if (source == maximizeButton)
                toggleMaximized();
            else if (source == closeButton)
                exit(0);
            else
                consume = false;
            if (consume)
                event.consume(); // should never reach hier.
        }
        logger.exiting("UndecoratedFXMLController", "onAction");
    }

    @Override
    void onMousePressed(MouseEvent event)
    {
        logger.entering("UndecoratedFXMLController", "onMousePressed", event);
        if (!event.isConsumed())
        {
            Object source = event.getSource();
            boolean isTitleBar = source == titlebarHbox;
            boolean isRoot = source == root && isResizeCursor(root.getCursor());
            boolean isValidSource = isRoot || isTitleBar;
            if (isValidSource && event.isPrimaryButtonDown())
            {
                mouseDragOffsetX = event.getScreenX();
                mouseDragOffsetY = event.getScreenY();
                event.consume();
            }
            else
            {
                mouseDragOffsetX = -1;
                mouseDragOffsetY = -1;
            }
        }
        logger.exiting("UndecoratedFXMLController", "onMousePressed");
    }

    @Override
    void onMouseDragged(MouseEvent event)
    {
        logger.entering("UndecoratedFXMLController", "onMouseDragged", event);
        if (!event.isConsumed())
        {
            Stage stage = getStage();
            Object source = event.getSource();
            boolean isTitleBar = source == titlebarHbox && !isResizeCursor(root.getCursor());
            boolean isValidOffset = mouseDragOffsetX != -1 && mouseDragOffsetY != -1;
            boolean canProcessMouseDrag = isValidOffset && !isFullScreen();
            canProcessMouseDrag = canProcessMouseDrag && event.isPrimaryButtonDown();
            if (isTitleBar && canProcessMouseDrag)
            {
                if (isMaximized())
                {
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
            }
            else if (source == root && canProcessMouseDrag && !isMaximized() && isWin7OrLater())
            {
                // Resize function is only test on windows
                // and is not by default supported
                // that is why this bloc was added.
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
                double x = stage.getX();
                double y = stage.getY();
                double width = stage.getWidth();
                double height = stage.getHeight();
                double newWidth = width + (west ? -1 : +1) * (event.getScreenX() - mouseDragOffsetX);
                double newHeight = height + (north ? -1 : +1) * (event.getScreenY() - mouseDragOffsetY);
                double newX = x + event.getScreenX() - mouseDragOffsetX;
                double newY = y + event.getScreenY() - mouseDragOffsetY;
                if ((north || south) && newHeight >= stage.getMinHeight())
                    stage.setHeight(newHeight);
                if ((east || west) && newWidth >= stage.getMinWidth())
                    stage.setWidth(newWidth);
                if (west && newX >= 0)
                    stage.setX(newX);
                if (north && newY >= 0)
                {
                    Rectangle2D rect = new Rectangle2D(x, y, width, height);
                    ObservableList<Screen> screensForRectangle;
                    screensForRectangle = Screen.getScreensForRectangle(rect);
                    if (screensForRectangle.size() > 0)
                    {
                        Screen screen = screensForRectangle.get(0);
                        Rectangle2D visualBounds = screen.getVisualBounds();
                        boolean canSetY = newY < visualBounds.getHeight() - 30;
                        canSetY = canSetY && newY >= visualBounds.getMinY();
                        if (canSetY)
                            stage.setY(newY);
                    }
                }
                mouseDragOffsetX = event.getScreenX();
                mouseDragOffsetY = event.getScreenY();
                event.consume();
            }
        }
        logger.exiting("UndecoratedFXMLController", "onMouseDragged");
    }

    @Override
    void onMouseClicked(MouseEvent event)
    {
        logger.entering("UndecoratedFXMLController", "onMouseClicked", event);
        if (!event.isConsumed())
        {
            Object source = event.getSource();
            Stage stage = getStage();
            boolean isValidSource = source == titlebarHbox;
            isValidSource = isValidSource && !isResizeCursor(root.getCursor());
            boolean isDoubleClick = event.getButton() == MouseButton.PRIMARY;
            isDoubleClick = isDoubleClick && event.getClickCount() == 2;
            if (isValidSource && isDoubleClick && !stage.isFullScreen())
            {
                toggleMaximized();
                event.consume();
            }
        }
        logger.exiting("UndecoratedFXMLController", "onMouseClicked");
    }

    @Override
    void onMouseMoved(MouseEvent event)
    {
        logger.entering("UndecoratedFXMLController", "onMouseMoved", event);
        if (!event.isConsumed())
        {
            Object source = event.getSource();
            if (source == root && !isMaximized() && !isFullScreen() && isWin7OrLater())
            {
                // Resize function is only test on windows
                // and is not by default supported
                // that is why this bloc was added.
                root.setCursor(Cursor.DEFAULT);
                // change the cursor for resize.
                double width = getStage().getWidth();
                double height = getStage().getHeight();
                double x = event.getSceneX() - width;
                double y = event.getSceneY() - height;
                boolean north = y <= -height + 3 && y >= -height;
                boolean east = x <= 0 && x >= -3;
                boolean south = y <= 0 && y >= -3;
                boolean west = x <= -width + 3 && x >= -width;
                boolean consume = true;
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
                else if (west)
                {
                    root.setCursor(Cursor.W_RESIZE);
                }
                else
                {
                    consume = false;
                }
                if (consume)
                    event.consume();
            }
        }
        logger.exiting("UndecoratedFXMLController", "onMouseMoved");
    }

    private void memorizeBounds()
    {
        Stage stage = getStage();
        double x = stage.getX();
        double y = stage.getY();
        double width = stage.getWidth();
        double height = stage.getHeight();
        savedBounds = new BoundingBox(x, y, width, height);
    }

    private void applySavedBounds()
    {
        Stage stage = getStage();
        stage.setX(savedBounds.getMinX());
        stage.setY(savedBounds.getMinY());
        stage.setWidth(savedBounds.getWidth());
        stage.setHeight(savedBounds.getHeight());
        savedBounds = null;
    }

    private void restoreWindowState()
    {
        if (configurator.getRememberWindowState())
        {
            Preferences prefs = configurator.getPrefs();
            savedBounds = readBounds(prefs, "xBound", "yBound", "wBound", "hBound");
            BoundingBox box = readBounds(prefs, "x", "y", "w", "h");
            maximized = prefs.getBoolean("maximized", false);
            boolean fullScreen = prefs.getBoolean("fullScreen", false);
            if (box != null)
            {
                getStage().setX(box.getMinX());
                getStage().setY(box.getMinY());
                getStage().setWidth(box.getWidth());
                getStage().setHeight(box.getHeight());
            }
            setFullScreen(fullScreen);
        }
    }

    private void saveWindowState()
    {
        if (configurator.getRememberWindowState())
        {
            Preferences prefs = configurator.getPrefs();
            writeBounds(savedBounds, prefs, "xBound", "yBound", "wBound", "hBound");
            BoundingBox box = new BoundingBox(getStage().getX(), getStage().getY(), getStage().getWidth(), getStage().getHeight());
            writeBounds(box, prefs, "x", "y", "w", "h");
            prefs.putBoolean("maximized", maximized);
            prefs.putBoolean("fullScreen", isFullScreen());
        }
    }

    private void writeBounds(BoundingBox box, Preferences prefs, String xKey, String yKey, String wKey, String hKey)
    {
        prefs.putDouble(xKey, box != null ? box.getMinX() : -1);
        prefs.putDouble(yKey, box != null ? box.getMinY() : -1);
        prefs.putDouble(wKey, box != null ? box.getWidth() : -1);
        prefs.putDouble(hKey, box != null ? box.getHeight() : -1);
    }

    private BoundingBox readBounds(Preferences prefs, String xKey, String yKey, String wKey, String hKey)
    {
        BoundingBox box = null;
        double x = prefs.getDouble(xKey, -1);
        double y = prefs.getDouble(yKey, -1);
        double width = prefs.getDouble(wKey, -1);
        double height = prefs.getDouble(hKey, -1);
        if (x != -1 && y != -1 && width != -1 && height != -1)
            box = new BoundingBox(x, y, width, height);
        return box;
    }
}
