package org.olympe.musicplayer.impl.fxml;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class AbstractFXMLController {

    private final Application application;
    private final Stage stage;
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;

    public AbstractFXMLController(Application application, Stage stage) {
        this.application = application;
        this.stage = stage;
    }

    public final Application getApplication() {
        return application;
    }

    public final Stage getStage() {
        return stage;
    }

    public final ResourceBundle getResources() {
        return resources;
    }

    public final URL getLocation() {
        return location;
    }

    @FXML
    abstract void initialize();

    //
    // Main Event Handlers.
    //

    @FXML
    abstract void onAction(ActionEvent event);

    //
    // Drag/Drop Event Handlers.
    //

    @FXML
    abstract void onDragDetected(MouseEvent event);

    @FXML
    abstract void onDragDone(DragEvent event);

    @FXML
    abstract void onDragDropped(DragEvent event);

    @FXML
    abstract void onDragEntered(DragEvent event);

    @FXML
    abstract void onDragExited(DragEvent event);

    @FXML
    abstract void onDragOver(DragEvent event);

    @FXML
    abstract void onMouseDragEntered(MouseEvent event);

    @FXML
    abstract void onMouseDragExited(MouseEvent event);

    @FXML
    abstract void onMouseDragOver(MouseEvent event);

    @FXML
    abstract void onMouseDragReleased(MouseEvent event);

    //
    // Keyboard Event Handlers.
    //

    @FXML
    abstract void onInputMethodTextChanged(InputMethodEvent event);

    @FXML
    abstract void onKeyPressed(KeyEvent event);

    @FXML
    abstract void onKeyReleased(KeyEvent event);

    @FXML
    abstract void onKeyTyped(KeyEvent event);

    //
    // Mouse Event Handlers.
    //

    @FXML
    abstract void onContextMenuRequested(ContextMenuEvent event);

    @FXML
    abstract void onMouseClicked(MouseEvent event);

    @FXML
    abstract void onMouseDragged(MouseEvent event);

    @FXML
    abstract void onMouseEntered(MouseEvent event);

    @FXML
    abstract void onMouseExited(MouseEvent event);

    @FXML
    abstract void onMouseMoved(MouseEvent event);

    @FXML
    abstract void onMousePressed(MouseEvent event);

    @FXML
    abstract void onMouseReleased(MouseEvent event);

    @FXML
    abstract void onScroll(ScrollEvent event);

    @FXML
    abstract void onScrollStarted(ScrollEvent event);

    @FXML
    abstract void onScrollFinished(ScrollEvent event);

    //
    // Rotation Event Handlers.
    //

    @FXML
    abstract void onRotate(RotateEvent event);

    @FXML
    abstract void onRotationStarted(RotateEvent event);

    @FXML
    abstract void onRotationFinished(RotateEvent event);

    //
    // Swipe Event Handlers.
    //

    @FXML
    abstract void onSwipeLeft(SwipeEvent event);

    @FXML
    abstract void onSwipeRight(SwipeEvent event);

    @FXML
    abstract void onSwipeUp(SwipeEvent event);

    @FXML
    abstract void onSwipeDown(SwipeEvent event);

    //
    // Touch Event Handlers.
    //

    @FXML
    abstract void onTouchMoved(TouchEvent event);

    @FXML
    abstract void onTouchPressed(TouchEvent event);

    @FXML
    abstract void onTouchReleased(TouchEvent event);

    @FXML
    abstract void onTouchStationary(TouchEvent event);

    //
    // Zoom Event Handlers.
    //

    @FXML
    abstract void onZoom(ZoomEvent event);

    @FXML
    abstract void onZoomStarted(ZoomEvent event);

    @FXML
    abstract void onZoomFinished(ZoomEvent event);
}
