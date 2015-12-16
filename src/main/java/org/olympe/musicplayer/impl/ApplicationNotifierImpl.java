package org.olympe.musicplayer.impl;

import com.sun.javafx.tk.Toolkit;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.olympe.musicplayer.impl.util.ApplicationNotifier;

public class ApplicationNotifierImpl implements ApplicationNotifier {

    StackPane overlayPane;
    BorderPane dialogPane;
    ButtonBar dialogButtonBar;
    HBox dialogHeaderPane;
    Label dialogHeaderLabel;

    private EventHandler<ActionEvent> actionEventHandler = event -> onAction(event);

    private Button okButton;

    @Override
    public void inform(String msg) {
        overlayPane.setVisible(true);
        FontAwesomeIconView graphic = new FontAwesomeIconView();
        graphic.setIcon(FontAwesomeIcon.INFO);
        dialogHeaderLabel.setGraphic(graphic);
        dialogHeaderLabel.setText("Information");
        dialogPane.setCenter(new Label(msg));
        if (okButton == null)
            createOkButton();
        dialogButtonBar.getButtons().setAll(okButton);
        Toolkit.getToolkit().enterNestedEventLoop(this);
    }

    private void onAction(ActionEvent event) {
        ButtonBar.ButtonData data = ButtonBar.getButtonData((Node) event.getSource());
        if (data != null) {
            Object ret = null;
            switch (data) {
                case OK_DONE:
                    ret = 0;
                    hideOverlayPane();
                    break;
                default:
                    break;
            }
            if (Toolkit.getToolkit().isNestedLoopRunning())
                Toolkit.getToolkit().exitNestedEventLoop(this, ret);
        }
    }

    private void createOkButton() {
        okButton = new Button("Ok");
        ButtonBar.setButtonData(okButton, ButtonBar.ButtonData.OK_DONE);
        okButton.setOnAction(actionEventHandler);
    }

    private void hideOverlayPane() {
        overlayPane.setVisible(false);
    }

    @Override
    public void warn(String msg) {

    }

    @Override
    public boolean ask(String question) {
        return false;
    }

    @Override
    public String askString(String prompt) {
        if (!Toolkit.getToolkit().canStartNestedEventLoop())
            return null;
        overlayPane.setVisible(true);
        FontAwesomeIconView graphic = new FontAwesomeIconView();
        graphic.setIcon(FontAwesomeIcon.QUESTION);
        dialogHeaderLabel.setGraphic(graphic);
        dialogHeaderLabel.setText("Question");
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        dialogPane.setCenter(textField);
        if (okButton == null)
            createOkButton();
        dialogButtonBar.getButtons().setAll(okButton);
        Object ret = Toolkit.getToolkit().enterNestedEventLoop(this);
        if (ret == null)
            return null;
        return textField.getText();
    }
}
