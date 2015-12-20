package org.olympe.musicplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.stage.Stage;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.id3.AbstractID3Tag;

import org.olympe.musicplayer.fxml.DefaultFXMLController;

public class MusicPlayerApplication extends Application
{
    private DefaultFXMLController controller;

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
        controller = new DefaultFXMLController(this, primaryStage);
        controller.getStage().show();
    }

    @Override
    public void stop() throws Exception
    {
        controller.exit(0);
    }

    private void applyDefaultLevel(Logger logger)
    {
        logger.setLevel(Level.WARNING);
    }
}
