package org.olympe.musicplayer.fxml;

import javafx.application.Application;
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
import javafx.stage.Stage;

public class DefaultFXMLController extends MusicPlayerFXMLController
{
    public DefaultFXMLController(Application application, Stage stage)
    {
        super(application, stage);
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
        super.initialize();
        collectOptions();
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
}
