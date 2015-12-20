package org.olympe.musicplayer.impl.fxml;

import com.sun.javafx.tk.Toolkit;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.control.PropertySheet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 */
public abstract class InternalNotifierFXMLController extends UndecoratedFXMLController {

    @FXML
    private StackPane overlayPane;
    @FXML
    private BorderPane notifierContainer;
    @FXML
    private ButtonBar notifierButtonBar;
    @FXML
    private HBox notifierHeader;
    @FXML
    private Label notifierHeaderLabel;

    private Button okButton;
    private Button closeButton;

    public InternalNotifierFXMLController(Application application, Stage stage) {
        super(application, stage);
    }

    @Override
    protected void showOptions(PropertySheet sheet) {
        String title = localize("Options.name");
        Node graphic = new FontAwesomeIconView(FontAwesomeIcon.COG);
        notify(title, graphic, sheet, ButtonData.CANCEL_CLOSE);
    }

    private Object notify(String title, Node graphic, Object content, ButtonData... buttons) {
        if (!Toolkit.getToolkit().canStartNestedEventLoop())
            return null;
        setNotifierVisible(true);
        setNotifierTitle(title);
        setNotifierGraphic(graphic);
        setNotifierContent(content);
        setNotifierButtons(buttons);
        return Toolkit.getToolkit().enterNestedEventLoop(this);
    }

    private void setNotifierVisible(boolean visible) {
        overlayPane.setVisible(visible);
    }

    private void setNotifierTitle(String title) {
        notifierHeaderLabel.setText(title);
    }

    private void setNotifierGraphic(Node graphic) {
        notifierHeaderLabel.setGraphic(graphic);
    }

    private void setNotifierContent(Object content) {
        Node node = null;
        if (content instanceof String) {
            Label lbl = new Label((String) content);
            lbl.setWrapText(true);
            node = lbl;
        } else if (content instanceof Node)
            node = (Node) content;
        notifierContainer.setCenter(node);
    }

    private void setNotifierButtons(ButtonData... notifierButtons) {
        List<Button> buttons = new ArrayList<>();
        Stream<ButtonData> stream = Stream.of(notifierButtons);
        stream.forEach(buttonData -> {
            buttons.add(getOrCreateButton(buttonData));
        });
        notifierButtonBar.getButtons().setAll(buttons);
    }

    private Button getOrCreateButton(ButtonData buttonData) {
        Button button = null;
        switch (buttonData) {
            case OK_DONE:
                button = getOrCreateOkButton();
                break;
            case CANCEL_CLOSE:
                button = getOrCreateCloseButton();
                break;
        }
        if (button == null)
            throw new IllegalStateException();
        return button;
    }

    private Button getOrCreateOkButton() {
        if (okButton == null) {
            okButton = new Button(localize("OkButton.name"));
            ButtonBar.setButtonData(okButton, ButtonData.OK_DONE);
            okButton.setOnAction(this::onAction);
        }
        return okButton;
    }

    private Button getOrCreateCloseButton() {
        if (closeButton == null) {
            closeButton = new Button(localize("CloseButton.name"));
            ButtonBar.setButtonData(closeButton, ButtonData.CANCEL_CLOSE);
            closeButton.setOnAction(this::onAction);
        }
        return closeButton;
    }
}
