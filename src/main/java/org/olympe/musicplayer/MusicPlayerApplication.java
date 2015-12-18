package org.olympe.musicplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.id3.AbstractID3Tag;
import org.olympe.musicplayer.impl.DefaultMusicPlayerController;
import org.olympe.musicplayer.impl.FXMLControllerWrapper;
import org.olympe.musicplayer.impl.fxml.FXMLController;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MusicPlayerApplication extends Application {

    private static final String FXML_NAME = "fxml/MusicPlayer.fxml";
    private static final String CSS_NAME = "css/default.css";
    private static final String I18N_NAME = "i18n.Messages";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        String[] loggerNames = {"org.jaudiotagger"};
        Logger[] loggers = new Logger[]{AudioFile.logger, AbstractID3Tag.logger};
        List<Logger> loggerList = new ArrayList<>(Arrays.asList(loggers));
        loggerList.addAll(Stream.of(loggerNames).map(Logger::getLogger).collect(Collectors.toList()));
        loggerList.parallelStream().forEach(logger -> logger.setLevel(Level.OFF));
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL location = ClassLoader.getSystemResource(FXML_NAME);
        FXMLLoader loader = new FXMLLoader(location);
        ResourceBundle resources = ResourceBundle.getBundle(I18N_NAME);
        loader.setResources(resources);
        MusicPlayerController controller = new DefaultMusicPlayerController();
        loader.setController(new FXMLController(this, primaryStage));
        AnchorPane root = loader.load();
        Scene scene = new Scene(root);
        location = ClassLoader.getSystemResource(CSS_NAME);
        scene.getStylesheets().add(location.toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }
}
