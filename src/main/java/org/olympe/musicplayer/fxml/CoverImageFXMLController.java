package org.olympe.musicplayer.fxml;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import de.androidpit.colorthief.ColorThief;
import jfxtras.labs.util.Util;
import org.olympe.musicplayer.bean.configurator.AppearanceConfigurator;
import org.olympe.musicplayer.bean.model.Audio;
import org.olympe.musicplayer.util.BeanPropertyWrapper;

/**
 *
 */
public abstract class CoverImageFXMLController extends MusicPlayerFXMLController
{
    private static ObservableMap<Object, Image> imageCache;
    private static ObservableMap<Color, String> styleSheetCache;

    static
    {
        imageCache = FXCollections.observableHashMap();
        styleSheetCache = FXCollections.observableHashMap();
    }

    @FXML
    private ImageView coverView;
    private AppearanceConfigurator configurator;

    public CoverImageFXMLController(Application application, Stage stage)
    {
        super(application, stage);
        configurator = new AppearanceConfigurator(getPreferencesNode("appearance"));
        configurator.colorProperty().addListener(this::fireDefaultThemeColorChanged);
        configurator.useCoverPredominantColorProperty().addListener(this::fireUsePredominantColorChanged);
        addExitHandler(configurator::saveToPreferences);
    }

    private static Image getImage(Artwork artwork)
    {
        logger.entering("CoverImageFXMLController", "getImage", artwork);
        Image image = null;
        if (artwork != null)
        {
            String url = artwork.getImageUrl();
            byte[] data = artwork.getBinaryData();
            Object source = (url != null) ? url : ((data != null) ? ByteBuffer.wrap(data) : null);
            if (source != null)
            {
                image = imageCache.get(source);
                if (image == null)
                {
                    if (source == url)
                    {
                        image = new Image(url);
                    }
                    else
                    {
                        image = new Image(new ByteArrayInputStream(data));
                    }
                    imageCache.put(source, image);
                }
            }
        }
        logger.exiting("CoverImageFXMLController", "getImage", image);
        return image;
    }

    private static Color getThemeColor(Image image)
    {
        Color color = null;
        BufferedImage img = SwingFXUtils.fromFXImage(image, null);
        int[] rgb = ColorThief.getColor(img);
        if (rgb != null)
        {
            color = Color.rgb(rgb[0], rgb[1], rgb[2]);
        }
        return color;
    }

    private static String getStyleSheet(Color color, String defaultStyleSheet)
    {
        String styleSheet = styleSheetCache.get(color);
        if (styleSheet == null && color != null)
        {
            String webColor = Util.colorToWebColor(color);
            try
            {
                File tmpFile = File.createTempFile("musicplayer-", ".css");
                tmpFile.deleteOnExit();
                URL url = new URL(defaultStyleSheet);
                FileWriter writer = new FileWriter(tmpFile);
                InputStreamReader in = new InputStreamReader(url.openStream());
                BufferedReader reader = new BufferedReader(in);
                reader.lines().forEach(line -> copyLine(writer, line, webColor));
                writer.close();
                in.close();
                reader.close();
                styleSheet = tmpFile.toURI().toURL().toExternalForm();
            }
            catch (IOException e)
            {
                logger.severe(e.getLocalizedMessage());
            }
            styleSheetCache.put(color, styleSheet);
        }
        return styleSheet;
    }

    private static void copyLine(FileWriter writer, String line, String webColor)
    {
        try
        {
            String pattern = "(\\s*)(-fx-base\\s*:\\s*)(#\\w+);";
            if (line.matches(pattern))
                line = line.replaceAll(pattern, "$1$2" + webColor + ";");
            writer.write(line);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void updateAudio(ObservableValue<? extends Audio> observable, Audio oldValue, Audio newValue)
    {
        logger.entering("CoverImageFXMLController", "updateAudio", new Object[]{observable, oldValue, newValue});
        super.updateAudio(observable, oldValue, newValue);
        coverView.setImage(null);
        if (newValue != null)
        {
            Tag tag = newValue.getTag();
            Artwork artwork = tag.getFirstArtwork();
            Image image = getImage(artwork);
            updateCoverImage(image);
        }
        updateThemeColor();
        logger.exiting("CoverImageFXMLController", "updateAudio");
    }

    protected abstract String getDefaultStyleSheet();

    @Override
    protected void collectOptions(ObservableList<PropertySheet.Item> options)
    {
        logger.entering("CoverImageFXMLController", "collectOptions", options);
        super.collectOptions(options);
        Stream<PropertySheet.Item> stream = BeanPropertyUtils.getProperties(configurator, this::isValidProperty).stream();
        stream = stream.map(BeanPropertyWrapper::new);
        options.addAll(stream.collect(Collectors.toList()));
        logger.exiting("CoverImageFXMLController", "collectOptions");
    }

    protected void setThemeColor(Color color)
    {
        String styleSheet = getStyleSheet(color, getDefaultStyleSheet());
        if (styleSheet == null)
            styleSheet = getDefaultStyleSheet();
        getStage().getScene().getStylesheets().setAll(styleSheet);
    }

    @Override
    void initialize()
    {
        super.initialize();
        Scene scene = getStage().getScene();
        coverView.fitWidthProperty().bind(scene.widthProperty());
        coverView.fitHeightProperty().bind(scene.heightProperty());
        Platform.runLater(this::updateThemeColor);
    }

    private void updateCoverImage(Image image)
    {
        if (image != null)
            coverView.setImage(image);
    }

    private void fireDefaultThemeColorChanged(ObservableValue<? extends Color> observable, Color oldValue, Color newValue)
    {
        updateThemeColor();
    }

    private Image getImage(Audio audio)
    {
        Image image = null;
        if (audio != null)
        {
            Tag tag = audio.getTag();
            Artwork artwork = tag.getFirstArtwork();
            if (artwork != null)
                image = getImage(artwork);
        }
        return image;
    }

    private void fireUsePredominantColorChanged(ObservableValue<? extends Boolean> obs, Boolean oldValue, Boolean newValue)
    {
        updateThemeColor();
    }

    private void updateThemeColor()
    {
        Image image = getImage(getLoadedAudio());
        Color color = configurator.getColor();
        if (configurator.getUseCoverPredominantColor() && image != null)
            color = getThemeColor(image);
        setThemeColor(color);
    }
}
