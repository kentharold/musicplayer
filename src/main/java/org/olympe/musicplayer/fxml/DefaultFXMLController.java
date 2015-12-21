package org.olympe.musicplayer.fxml;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.RotateEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.olympe.musicplayer.bean.model.Audio;

public class DefaultFXMLController extends CoverImageFXMLController
{
    private static final String FXML_NAME = "fxml/MusicPlayer.fxml";
    private static final String CSS_NAME = "css/default.css";
    private static final String I18N_NAME = "i18n.Messages";

    public DefaultFXMLController(Application application, Stage stage) throws IOException
    {
        super(application, stage);
        URL location = ClassLoader.getSystemResource(FXML_NAME);
        FXMLLoader loader = new FXMLLoader(location);
        ResourceBundle resources = ResourceBundle.getBundle(I18N_NAME);
        loader.setResources(resources);
        loader.setController(this);
        Parent busyNode = new BorderPane(new Label("loading the application"));
        Scene scene = new Scene(busyNode, 600, 480);
        scene.getStylesheets().add(getDefaultStyleSheet());
        stage.setScene(scene);
        stage.setTitle("Olympe Music Player");
        Platform.runLater(() -> initialize(scene, loader));
    }

    @Override
    protected String getDefaultStyleSheet()
    {
        URL css = ClassLoader.getSystemResource(CSS_NAME);
        return css.toExternalForm();
    }

    @Override
    protected void unregisterAudio(Audio audio)
    {
    }

    @Override
    protected void registerAudio(Audio audio)
    {
    }

    @Override
    protected void onStopped()
    {
    }

    @Override
    protected void onStalled()
    {
    }

    @Override
    protected void onRepeat()
    {
    }

    @Override
    protected void onReady()
    {
    }

    @Override
    protected void onPlaying()
    {
    }

    @Override
    protected void onPaused()
    {
    }

    @Override
    protected void onHalted()
    {
    }

    @Override
    protected void onErrorMedia()
    {
    }

    @Override
    void initialize()
    {
        logger.entering("DefaultFXMLController", "initialize");
        super.initialize();
        collectOptions();
        logger.exiting("DefaultFXMLController", "initialize");
    }

    @Override
    void onDragDetected(MouseEvent event)
    {
    }

    @Override
    void onDragDone(DragEvent event)
    {
    }

    @Override
    void onDragEntered(DragEvent event)
    {
    }

    @Override
    void onDragExited(DragEvent event)
    {
    }

    @Override
    void onMouseDragEntered(MouseEvent event)
    {
    }

    @Override
    void onMouseDragExited(MouseEvent event)
    {
    }

    @Override
    void onMouseDragOver(MouseEvent event)
    {
    }

    @Override
    void onMouseDragReleased(MouseEvent event)
    {
    }

    @Override
    void onInputMethodTextChanged(InputMethodEvent event)
    {
    }

    @Override
    void onKeyPressed(KeyEvent event)
    {
    }

    @Override
    void onKeyReleased(KeyEvent event)
    {
    }

    @Override
    void onKeyTyped(KeyEvent event)
    {
    }

    @Override
    void onContextMenuRequested(ContextMenuEvent event)
    {
    }

    @Override
    void onMouseEntered(MouseEvent event)
    {
    }

    @Override
    void onMouseExited(MouseEvent event)
    {
    }

    @Override
    void onMouseReleased(MouseEvent event)
    {
    }

    @Override
    void onScroll(ScrollEvent event)
    {
    }

    @Override
    void onScrollStarted(ScrollEvent event)
    {
    }

    @Override
    void onScrollFinished(ScrollEvent event)
    {
    }

    @Override
    void onRotate(RotateEvent event)
    {
    }

    @Override
    void onRotationStarted(RotateEvent event)
    {
    }

    @Override
    void onRotationFinished(RotateEvent event)
    {
    }

    @Override
    void onSwipeLeft(SwipeEvent event)
    {
    }

    @Override
    void onSwipeRight(SwipeEvent event)
    {
    }

    @Override
    void onSwipeUp(SwipeEvent event)
    {
    }

    @Override
    void onSwipeDown(SwipeEvent event)
    {
    }

    @Override
    void onTouchMoved(TouchEvent event)
    {
    }

    @Override
    void onTouchPressed(TouchEvent event)
    {
    }

    @Override
    void onTouchReleased(TouchEvent event)
    {
    }

    @Override
    void onTouchStationary(TouchEvent event)
    {
    }

    @Override
    void onZoom(ZoomEvent event)
    {
    }

    @Override
    void onZoomStarted(ZoomEvent event)
    {
    }

    @Override
    void onZoomFinished(ZoomEvent event)
    {
    }

    private void initialize(Scene scene, FXMLLoader loader)
    {
        logger.entering("DefaultFXMLController", "initialize", new Object[]{scene, loader});
        try
        {
            Parent root = loader.load();
            scene.setRoot(root);
        }
        catch (IOException e)
        {
            logger.severe(e.getLocalizedMessage());
            exit(1);
        }
        logger.exiting("DefaultFXMLController", "initialize");
    }
}
