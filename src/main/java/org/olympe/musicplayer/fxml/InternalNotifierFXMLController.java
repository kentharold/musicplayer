package org.olympe.musicplayer.fxml;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.event.ActionEvent;
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

import com.sun.javafx.tk.Toolkit;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

/**
 *
 */
public abstract class InternalNotifierFXMLController extends UndecoratedFXMLController
{
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

    public InternalNotifierFXMLController(Application application, Stage stage)
    {
        super(application, stage);
    }

    @Override
    protected void showOptions(PropertySheet sheet)
    {
        logger.entering("InternalNotifierFXMLController", "showOptions", sheet);
        String title = localize("Options.name");
        Node graphic = new FontAwesomeIconView(FontAwesomeIcon.COG);
        notify(title, graphic, sheet, ButtonData.CANCEL_CLOSE);
        logger.exiting("InternalNotifierFXMLController", "showOptions");
    }

    @Override
    void onAction(ActionEvent event)
    {
        logger.entering("InternalNotifierFXMLController", "onAction", event);
        super.onAction(event);
        if (event.isConsumed())
            return;
        Object source = event.getSource();
        if (source == okButton)
        {
            // TODO: return the asked value.
        }
        Object ret = null;
        if (source == okButton || source == closeButton)
        {
            setNotifierVisible(false);
            Toolkit.getToolkit().exitNestedEventLoop(this, ret);
            event.consume();
        }
        logger.exiting("InternalNotifierFXMLController", "onAction");
    }

    private Object notify(String title, Node graphic, Object content, ButtonData... buttons)
    {
        logger.entering("InternalNotifierFXMLController", "notify", new Object[]{title, graphic, content, buttons});
        if (!Toolkit.getToolkit().canStartNestedEventLoop())
            throw new IllegalStateException();
        Object result = null;
        if (!Toolkit.getToolkit().isNestedLoopRunning())
        {
            setNotifierVisible(true);
            setNotifierTitle(title);
            setNotifierGraphic(graphic);
            setNotifierContent(content);
            setNotifierButtons(buttons);
            result = Toolkit.getToolkit().enterNestedEventLoop(this);
        }
        else
        {
            logger.warning("the notifier is already running.");
        }
        logger.exiting("InternalNotifierFXMLController", "notify", result);
        return result;
    }

    private void setNotifierVisible(boolean visible)
    {
        logger.entering("InternalNotifierFXMLController", "setNotifierVisible", visible);
        overlayPane.setVisible(visible);
        logger.exiting("InternalNotifierFXMLController", "setNotifierVisible");
    }

    private void setNotifierTitle(String title)
    {
        logger.entering("InternalNotifierFXMLController", "setNotifierTitle", title);
        notifierHeaderLabel.setText(title);
        logger.exiting("InternalNotifierFXMLController", "setNotifierTitle");
    }

    private void setNotifierGraphic(Node graphic)
    {
        logger.entering("InternalNotifierFXMLController", "setNotifierGraphic", graphic);
        notifierHeaderLabel.setGraphic(graphic);
        logger.exiting("InternalNotifierFXMLController", "setNotifierGraphic");
    }

    private void setNotifierContent(Object content)
    {
        logger.entering("InternalNotifierFXMLController", "setNotifierContent", content);
        Node node = null;
        if (content instanceof String)
        {
            Label lbl = new Label((String) content);
            lbl.setWrapText(true);
            node = lbl;
        }
        else if (content instanceof Node)
            node = (Node) content;
        notifierContainer.setCenter(node);
        logger.exiting("InternalNotifierFXMLController", "setNotifierContent");
    }

    private void setNotifierButtons(ButtonData... notifierButtons)
    {
        logger.entering("InternalNotifierFXMLController", "setNotifierButtons", notifierButtons);
        List<Button> buttons = new ArrayList<>();
        Stream<ButtonData> stream = Stream.of(notifierButtons);
        stream.forEach(buttonData -> buttons.add(getOrCreateButton(buttonData)));
        notifierButtonBar.getButtons().setAll(buttons);
        logger.exiting("InternalNotifierFXMLController", "setNotifierButtons");
    }

    private Button getOrCreateButton(ButtonData buttonData)
    {
        logger.entering("InternalNotifierFXMLController", "getOrCreateButton", buttonData);
        Button button = null;
        switch (buttonData)
        {
            case OK_DONE:
                button = getOrCreateOkButton();
                break;
            case CANCEL_CLOSE:
                button = getOrCreateCloseButton();
                break;
        }
        assert button != null;
        logger.exiting("InternalNotifierFXMLController", "getOrCreateButton", button);
        return button;
    }

    private Button getOrCreateOkButton()
    {
        logger.entering("InternalNotifierFXMLController", "getOrCreateOkButton");
        if (okButton == null)
        {
            okButton = new Button(localize("OkButton.name"));
            ButtonBar.setButtonData(okButton, ButtonData.OK_DONE);
            okButton.setOnAction(this::onAction);
        }
        logger.exiting("InternalNotifierFXMLController", "getOrCreateOkButton", okButton);
        return okButton;
    }

    private Button getOrCreateCloseButton()
    {
        logger.entering("InternalNotifierFXMLController", "getOrCreateCloseButton");
        if (closeButton == null)
        {
            closeButton = new Button(localize("CloseButton.name"));
            ButtonBar.setButtonData(closeButton, ButtonData.CANCEL_CLOSE);
            closeButton.setOnAction(this::onAction);
        }
        logger.exiting("InternalNotifierFXMLController", "getOrCreateCloseButton", closeButton);
        return closeButton;
    }
}
