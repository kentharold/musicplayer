package org.olympe.musicplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.olympe.musicplayer.impl.DefaultMusicPlayerController;
import org.olympe.musicplayer.util.FXMLControllerWrapper;

import java.io.IOException;
import java.net.URL;

public class MusicPlayerApplication extends Application {

    private static final String FXML_NAME = "fxml/MusicPlayer.fxml";
    private static final String CSS_NAME = "css/default.css";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL location = ClassLoader.getSystemResource(FXML_NAME);
        FXMLLoader loader = new FXMLLoader(location);
        MusicPlayerController controller = new DefaultMusicPlayerController();
        loader.setController(new FXMLControllerWrapper(controller, primaryStage));
        VBox root = loader.load();
        Scene scene = new Scene(root);
        location = ClassLoader.getSystemResource(CSS_NAME);
        scene.getStylesheets().add(location.toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);

        primaryStage.show();
    }
}
