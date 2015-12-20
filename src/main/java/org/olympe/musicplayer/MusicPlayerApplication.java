package org.olympe.musicplayer;

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

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.id3.AbstractID3Tag;

import org.olympe.musicplayer.fxml.DefaultFXMLController;

public class MusicPlayerApplication extends Application
{
    private static final String FXML_NAME = "fxml/MusicPlayer.fxml";
    private static final String CSS_NAME = "css/default.css";
    private static final String I18N_NAME = "i18n.Messages";

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void init() throws Exception
    {
        String[] loggerNames = {"org.jaudiotagger"};
        Logger[] loggers = new Logger[]{AudioFile.logger, AbstractID3Tag.logger};
        List<Logger> loggerList = new ArrayList<>(Arrays.asList(loggers));
        Stream<Logger> stream = Stream.of(loggerNames).map(Logger::getLogger);
        loggerList.addAll(stream.collect(Collectors.toList()));
        loggerList.parallelStream().forEach(this::applyDefaultLevel);
    }

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        URL location = ClassLoader.getSystemResource(FXML_NAME);
        FXMLLoader loader = new FXMLLoader(location);
        ResourceBundle resources = ResourceBundle.getBundle(I18N_NAME);
        loader.setResources(resources);
        loader.setController(new DefaultFXMLController(this, primaryStage));
        StackPane root = loader.load();
        Scene scene = new Scene(root);
        location = ClassLoader.getSystemResource(CSS_NAME);
        scene.getStylesheets().add(location.toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    private void applyDefaultLevel(Logger logger)
    {
        logger.setLevel(Level.WARNING);
    }
}
